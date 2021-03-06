package wtf.emulator;

import com.android.build.VariantOutput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApkVariant;
import com.android.build.gradle.api.ApkVariantOutput;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.api.TestedVariant;
import com.vdurmont.semver4j.Semver;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.initialization.resolve.RepositoriesMode;
import org.gradle.api.internal.GradleInternal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EwPlugin implements Plugin<Project> {
  private static final String TOOL_CONFIGURATION = "emulatorWtfCli";

  private static final String MAVEN_URL = "https://maven.emulator.wtf/releases/";

  @Override
  public void apply(Project target) {
    EwExtension ext = target.getExtensions().create("emulatorwtf", EwExtension.class);

    // setup defaults
    ext.getBaseOutputDir().convention(target.getLayout().getBuildDirectory().dir("test-results"));
    ext.getRepositoryCheckEnabled().convention(true);

    configureRepository(target, ext);
    final Configuration toolConfig = target.getConfigurations().maybeCreate(TOOL_CONFIGURATION);
    target.getDependencies().add(TOOL_CONFIGURATION, ext.getVersion().map(version ->
        "wtf.emulator:ew-cli:" + version));

    // configure application builds
    target.getPluginManager().withPlugin("com.android.application", (plugin) -> {
      AppExtension android = target.getExtensions().getByType(AppExtension.class);
      android.getApplicationVariants().all(variant -> configureVariant(target, android, ext, toolConfig, variant));
    });

    // configure library builds
    target.getPluginManager().withPlugin("com.android.library", (plugin) -> {
      LibraryExtension android = target.getExtensions().getByType(LibraryExtension.class);
      android.getLibraryVariants().all(variant -> configureVariant(target, android, ext, toolConfig, variant));
    });

    //TODO(madis) configure feature builds & separate test project builds
  }

  private static void configureRepository(Project target, EwExtension ext) {
    Semver gradleVersion = new Semver(target.getGradle().getGradleVersion(), Semver.SemverType.LOOSE);
    if (gradleVersion.isGreaterThanOrEqualTo(new Semver("6.8", Semver.SemverType.LOOSE))) {
      // TODO(madis) yuck
      // https://github.com/gradle/gradle/issues/17295
      Settings settings = ((GradleInternal) target.getGradle()).getSettings();

      DependencyResolutionManagement mgmt = settings.getDependencyResolutionManagement();
      boolean registered = mgmt.getRepositories().stream()
          .filter(artifactRepository -> artifactRepository instanceof MavenArtifactRepository)
          .map(artifactRepository -> (MavenArtifactRepository)artifactRepository)
          .anyMatch(EwPlugin::isEmulatorWtfRepo);

      if (registered) {
        return;
      }

      RepositoriesMode mode = settings.getDependencyResolutionManagement().getRepositoriesMode().getOrNull();
      if (mode == null || RepositoriesMode.PREFER_PROJECT.equals(mode)) {
        registerMavenRepo(target);
      } else {
        // ping user after project evaluate to allow suppressing this check in dsl
        target.afterEvaluate(evaluated -> {
          if (Boolean.TRUE.equals(ext.getRepositoryCheckEnabled().getOrNull())) {
            throw new GradleException("Missing maven.emulator.wtf repository\n\n" +
                "Either add the following to your dependencyResolutionManagement dependencies block or\n" +
                "suppress this message via emulatorWtf { repositoryCheckEnabled.set(false) }:\n\n" +
                "dependencyResolutionManagement {\n" +
                "  repositories {\n" +
                "    maven(url = \"https://maven.emulator.wtf/releases/\") {\n" +
                "      content { includeGroup(\"wtf.emulator\") }\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
          }
        });
      }
    } else {
      registerMavenRepo(target);
    }
  }

  private static boolean isEmulatorWtfRepo(MavenArtifactRepository it) {
    return MAVEN_URL.equals(it.getUrl().toString()) || MAVEN_URL.equals(it.getUrl() + "/");
  }

  private static void registerMavenRepo(Project target) {
    target.getRepositories().maven(repo -> {
      try {
        repo.setUrl(new URI(MAVEN_URL).toURL());
      } catch (MalformedURLException | URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  public <T extends TestedVariant & BaseVariant> void configureVariant(Project target, BaseExtension android, EwExtension ext, Configuration toolConfig, T variant) {
    String taskName = "test" + capitalize(variant.getName()) + "WithEmulatorWtf";

    TestVariant testVariant = variant.getTestVariant();

    if (testVariant != null) {
      target.getTasks().register(taskName, EwExecTask.class, task -> {
        task.setDescription("Run " + variant.getName() + " instrumentation tests with emulator.wtf");
        task.setGroup("Verification");

        task.getClasspath().set(toolConfig);

        task.getToken().set(ext.getToken().orElse(target.provider(() ->
            System.getenv("EW_API_TOKEN"))));

        task.dependsOn(testVariant.getPackageApplicationProvider());

        // TODO(madis) we could do better than main here, technically we do know the list of
        //             devices we're going to run against..
        BaseVariantOutput appOutput = getMainOutput(testVariant.getTestedVariant());
        BaseVariantOutput testOutput = getMainOutput(testVariant);

        if (appOutput instanceof ApkVariantOutput) {
          task.dependsOn(((ApkVariant) variant).getPackageApplicationProvider());

          task.getAppApk().set(appOutput.getOutputFile());
          task.getTestApk().set(testOutput.getOutputFile());
        } else {
          task.getLibraryTestApk().set(testOutput.getOutputFile());
        }

        task.getOutputsDir().set(ext.getBaseOutputDir().map(dir -> dir.dir(variant.getName())));

        task.getDevices().set(ext.getDevices().map(devices -> devices.stream().map((config) -> {
          final Map<String, String> out = new HashMap<>();
          config.forEach((key, value) -> out.put(key, Objects.toString(value)));
          return out;
        }).collect(Collectors.toList())));

        task.getUseOrchestrator().set(ext.getUseOrchestrator().orElse(target.provider(() ->
            android.getTestOptions().getExecution().equalsIgnoreCase("ANDROIDX_TEST_ORCHESTRATOR"))));

        task.getClearPackageData().set(ext.getClearPackageData());

        task.getWithCoverage().set(ext.getWithCoverage().orElse(target.provider(() ->
            variant.getBuildType().isTestCoverageEnabled())));

        task.getAdditionalApks().set(ext.getAdditionalApks());

        task.getEnvironmentVariables().set(ext.getEnvironmentVariables()
            .map((entries) -> {
              // pick defaults from test instrumentation runner args, then fill with overrides
              final Map<String, String> out = new HashMap<>(
                  variant.getMergedFlavor().getTestInstrumentationRunnerArguments());
              entries.forEach((key, value) -> out.put(key, Objects.toString(value)));
              return out;
            }));

        task.getNumUniformShards().set(ext.getNumUniformShards());
        task.getNumShards().set(ext.getNumShards());
        task.getDirectoriesToPull().set(ext.getDirectoriesToPull());
      });
    }
  }

  private static BaseVariantOutput getMainOutput(BaseVariant variant) {
    return variant.getOutputs().stream().filter(it -> it.getOutputType().equals(VariantOutput.MAIN))
        .findFirst().orElse(variant.getOutputs().stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Test variant has no outputs!")
          ));
  }

  private static String capitalize(String str) {
    if (str.length() == 0) {
      return str;
    }
    return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
  }
}
