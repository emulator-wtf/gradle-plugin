package wtf.emulator;

import com.android.build.VariantOutput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.TestExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.api.TestedVariant;
import com.vdurmont.semver4j.Semver;

import org.apache.commons.io.FileUtils;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.initialization.resolve.RepositoriesMode;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EwPlugin implements Plugin<Project> {
  private static final String ROOT_TASK_NAME = "testWithEmulatorWtf";

  private static final String TOOL_CONFIGURATION = "emulatorWtfCli";

  private static final String MAVEN_URL = "https://maven.emulator.wtf/releases/";

  @Override
  public void apply(Project target) {
    EwExtension ext = target.getExtensions().create("emulatorwtf", EwExtension.class);
    GradleCompat gradleCompat = GradleCompatFactory.get(target.getGradle());

    // setup defaults
    ext.getBaseOutputDir().convention(target.getLayout().getBuildDirectory().dir("test-results"));
    ext.getRepositoryCheckEnabled().convention(true);

    configureRepository(target, ext);
    final Configuration toolConfig = target.getConfigurations().maybeCreate(TOOL_CONFIGURATION);
    gradleCompat.addProviderDependency(target, TOOL_CONFIGURATION, ext.getVersion().map(version -> "wtf.emulator:ew-cli:" + version));

    Boolean configCache = gradleCompat.isConfigurationCacheEnabled();

    SetProperty<String> failureCollector = target.getObjects().setProperty(String.class);

    // create root anchor task
    TaskProvider<EwExecSummaryTask> rootTask = target.getTasks().register(ROOT_TASK_NAME, EwExecSummaryTask.class, task -> {
      task.setDescription("Run instrumentation tests of all variants with emulator.wtf");
      task.getPrintingEnabled().set(ext.getIgnoreFailures().map(ignoreFailures -> configCache && ignoreFailures));
      // summary task is never up-to-date
      task.getOutputs().upToDateWhen(it -> false);
    });

    if (!configCache) {
      target.getGradle().addBuildListener(new BuildAdapter() {
        @Override
        @SuppressWarnings("deprecation")
        public void buildFinished(BuildResult result) {
          if (ext.getIgnoreFailures().getOrElse(false)) {
            List<String> failures = failureCollector.get().stream().filter(it -> it != null && !it.isEmpty()).collect(Collectors.toList());
            if (!failures.isEmpty()) {
              target.getLogger().error("");
              failures.forEach(target.getLogger()::error);
            }
          }
        }
      });
    }

    // configure application builds
    target.getPluginManager().withPlugin("com.android.application", plugin -> {
      AppExtension android = target.getExtensions().getByType(AppExtension.class);
      android.getApplicationVariants().all(variant -> configureAppVariant(target, android, ext, toolConfig, rootTask, failureCollector, variant));
    });

    // configure library builds
    target.getPluginManager().withPlugin("com.android.library", plugin -> {
      LibraryExtension android = target.getExtensions().getByType(LibraryExtension.class);
      android.getLibraryVariants().all(variant -> configureLibraryVariant(target, android, ext, toolConfig, rootTask, failureCollector, variant));
    });

    // configure test project builds
    target.getPluginManager().withPlugin("com.android.test", plugin -> {
      TestExtension android = target.getExtensions().getByType(TestExtension.class);
      android.getApplicationVariants().all(variant -> configureTestVariant(target, android, ext, toolConfig, rootTask, failureCollector, variant));
    });

    //TODO(madis) configure feature builds
  }

  private static void configureRepository(Project target, EwExtension ext) {
    if (!EwProperties.ADD_REPOSITORY.getFlag(target, true)) {
      return;
    }

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
      int settingsRepoCount = settings.getDependencyResolutionManagement().getRepositories().size();
      if ((mode == null || mode == RepositoriesMode.PREFER_PROJECT) && settingsRepoCount == 0) {
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
        repo.mavenContent((desc) -> desc.includeGroup("wtf.emulator"));
      } catch (MalformedURLException | URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  public static void configureAppVariant(Project target, BaseExtension android, EwExtension ext, Configuration toolConfig, TaskProvider<EwExecSummaryTask> rootTask, SetProperty<String> failureCollector, ApplicationVariant variant) {
    TestVariant testVariant = variant.getTestVariant();
    if (testVariant != null) {
      configureEwTask(target, android, ext, toolConfig, rootTask, failureCollector, variant, task -> {
        // TODO(madis) we could do better than main here, technically we do know the list of
        //             devices we're going to run against..
        BaseVariantOutput appOutput = getMainOutput(testVariant.getTestedVariant());
        BaseVariantOutput testOutput = getMainOutput(testVariant);

        task.dependsOn(testVariant.getPackageApplicationProvider());
        task.dependsOn(variant.getPackageApplicationProvider());

        task.getAppApk().set(appOutput.getOutputFile());
        task.getTestApk().set(testOutput.getOutputFile());
      });
    }
  }

  public static void configureLibraryVariant(Project target, BaseExtension android, EwExtension ext, Configuration toolConfig, TaskProvider<EwExecSummaryTask> rootTask, SetProperty<String> failureCollector, LibraryVariant variant) {
    TestVariant testVariant = variant.getTestVariant();
    if (testVariant != null) {
      configureEwTask(target, android, ext, toolConfig, rootTask, failureCollector, variant, task -> {
        // library projects only have the test apk
        BaseVariantOutput testOutput = getMainOutput(testVariant);
        task.dependsOn(testVariant.getPackageApplicationProvider());
        task.getLibraryTestApk().set(testOutput.getOutputFile());
      });
    }
  }

  public static void configureTestVariant(Project project, TestExtension android, EwExtension ext, Configuration toolConfig, TaskProvider<EwExecSummaryTask> rootTask, SetProperty<String> failureCollector, ApplicationVariant variant) {
    configureEwTask(project, android, ext, toolConfig, rootTask, failureCollector, variant, task -> {
      // test projects have the test apk as a main output
      BaseVariantOutput testOutput = getMainOutput(variant);
      task.dependsOn(variant.getPackageApplicationProvider());
      task.getTestApk().set(testOutput.getOutputFile());

      // look up the referenced target variant
      String targetProjectPath = android.getTargetProjectPath();
      Project target = project.getRootProject().findProject(targetProjectPath);
      if (target == null) {
        throw new IllegalArgumentException("No target project '" + targetProjectPath + "'");
      }
      target.getPluginManager().withPlugin("com.android.application", (plugin) -> {
        AppExtension targetAndroid = target.getExtensions().getByType(AppExtension.class);
        targetAndroid.getApplicationVariants().all(targetVariant -> {
          // direct variant <-> variant matching between the two
          if (variant.getName().equals(targetVariant.getName())) {
            BaseVariantOutput appOutput = getMainOutput(targetVariant);
            task.dependsOn(targetVariant.getPackageApplicationProvider());
            task.getAppApk().set(appOutput.getOutputFile());
          }
        });
      });
    });
  }

  private static <T extends TestedVariant & BaseVariant> void configureEwTask(
      Project target,
      BaseExtension android,
      EwExtension ext,
      Configuration toolConfig,
      TaskProvider<EwExecSummaryTask> rootTask,
      SetProperty<String> failureCollector,
      T variant,
      Consumer<EwExecTask> additionalConfigure
  ) {

    Action<EwVariantFilter> filter = ext.getFilter();
    if (filter != null) {
      EwVariantFilter filterSpec = new EwVariantFilter(variant);
      filter.execute(filterSpec);
      if (!filterSpec.isEnabled()) {
        return;
      }
    }

    // bump the variant count
    ext.getVariantCount().set(ext.getVariantCount().get() + 1);

    // create failure property for each variant
    Path intermediateFolder = target.getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf");
    File outputFailureFile = intermediateFolder.resolve("failure_" + variant.getName() + ".txt").toFile();
    Provider<String> outputFailure = target.provider(() -> {
      try {
        if (outputFailureFile.exists()) {
          return FileUtils.readFileToString(outputFailureFile, StandardCharsets.UTF_8);
        }
      } catch (IOException ioe) {
        /* ignore */
      }
      return "";
    });
    failureCollector.add(outputFailure);
    rootTask.configure(task -> task.getFailureMessages().add(outputFailureFile));

    // register the work task
    String taskName = "test" + capitalize(variant.getName()) + "WithEmulatorWtf";
    TaskProvider<EwExecTask> execTask = target.getTasks().register(taskName, EwExecTask.class, task -> {
      task.setDescription("Run " + variant.getName() + " instrumentation tests with emulator.wtf");
      task.setGroup("Verification");

      if (ext.getSideEffects().isPresent() && ext.getSideEffects().get()) {
        task.getOutputs().upToDateWhen((t) -> false);
        task.getSideEffects().set(true);
      }

      task.getClasspath().set(toolConfig);

      task.getToken().set(ext.getToken().orElse(target.provider(() ->
          System.getenv("EW_API_TOKEN"))));

      // don't configure outputs in async mode
      if (!task.getAsync().getOrElse(false)) {
        task.getOutputsDir().set(ext.getBaseOutputDir().map(dir -> dir.dir(variant.getName())));
        task.getOutputTypes().set(ext.getOutputs());
      }

      task.getRecordVideo().set(ext.getRecordVideo());

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
      task.getNumBalancedShards().set(ext.getNumBalancedShards());
      task.getDirectoriesToPull().set(ext.getDirectoriesToPull());

      task.getTestTimeout().set(ext.getTimeout());

      task.getFileCacheEnabled().set(ext.getFileCacheEnabled());
      task.getFileCacheTtl().set(ext.getFileCacheTtl());

      task.getTestCacheEnabled().set(ext.getTestCacheEnabled());

      task.getNumFlakyTestAttempts().set(ext.getNumFlakyTestAttempts());

      task.getScmUrl().set(ext.getScmUrl());
      task.getScmCommitHash().set(ext.getScmCommitHash());


      task.getDisplayName().set(ext.getDisplayName().orElse(ext.getVariantCount().map((count) -> {
        String name = task.getProject().getPath();
        if (name.equals(":")) {
          // replace with rootProject name
          name = task.getProject().getName();
        }
        if (count < 2) {
          return name;
        } else {
          return name + ":" + variant.getName();
        }
      })));

      task.getWorkingDir().set(target.getRootProject().getRootDir());

      task.getOutputFailureFile().set(outputFailureFile);

      task.getIgnoreFailures().set(ext.getIgnoreFailures());

      task.getAsync().set(ext.getAsync());

      additionalConfigure.accept(task);
    });

    rootTask.configure(task -> task.dependsOn(execTask));
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
