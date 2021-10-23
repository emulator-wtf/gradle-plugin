package wtf.emulator;

import com.android.build.VariantOutput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApkVariant;
import com.android.build.gradle.api.ApkVariantOutput;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.api.TestedVariant;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.JavaExec;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EwPlugin implements Plugin<Project> {
  private static final String TOOL_CONFIGURATION = "emulatorWtfCli";

  @Override
  public void apply(Project target) {
    EwExtension ext = target.getExtensions().create("emulatorwtf", EwExtension.class);
    ext.getBaseOutputDir().convention(target.getLayout().getBuildDirectory().dir("test-results"));

    target.getRepositories().maven(repo -> {
      try {
        repo.setUrl(new URI("https://maven.emulator.wtf/releases/").toURL());
      } catch (MalformedURLException | URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    });

    final Configuration toolConfig = target.getConfigurations().maybeCreate(TOOL_CONFIGURATION);

    target.getDependencies().add(TOOL_CONFIGURATION, ext.getVersion().map(version ->
        "wtf.emulator:ew-cli:" + version));

    target.getPluginManager().withPlugin("com.android.application", (plugin) ->
      target.getExtensions().getByType(AppExtension.class)
          .getApplicationVariants().all(variant -> configureVariant(target, ext, toolConfig, variant))
    );
    target.getPluginManager().withPlugin("com.android.library", (plugin) ->
      target.getExtensions().getByType(LibraryExtension.class)
          .getLibraryVariants().all(variant -> configureVariant(target, ext, toolConfig, variant)));
  }

  public <T extends TestedVariant & BaseVariant> void configureVariant(Project target, EwExtension ext, Configuration toolConfig, T variant) {
    String taskName = "test" + capitalize(variant.getName()) + "WithEmulatorWtf";

    TestVariant testVariant = variant.getTestVariant();

    if (testVariant != null) {
      target.getTasks().register(taskName, JavaExec.class, task -> {
        task.setDescription("Run " + variant.getName() + " instrumentation tests with emulator.wtf");
        task.setGroup("Verification");

        task.dependsOn(testVariant.getPackageApplicationProvider());
        task.classpath(toolConfig);

        // TODO(madis) we could do better than main here, technically we do know the list of
        //             devices we're going to run against..
        BaseVariantOutput appOutput = getMainOutput(testVariant.getTestedVariant());

        if (appOutput instanceof ApkVariantOutput) {
          task.dependsOn(((ApkVariant) variant).getPackageApplicationProvider());

          String underTest = appOutput.getOutputFile().getAbsolutePath();

          if (underTest.endsWith(".apk")) {
            task.args("--app", underTest);
          }
        }

        BaseVariantOutput testOutput = getMainOutput(testVariant);

        String token = ext.getToken().getOrNull();
        if (token == null) {
          token = System.getenv("EW_API_TOKEN");
        }

        if (token == null) {
          throw new IllegalArgumentException("Missing token for emulator.wtf.\n" +
              "Did you forgot to set token in the emulatorwtf {} block?");
        }

        // use env var for token so it doesn't get logged out with --info
        task.environment("EW_API_TOKEN", token);

        task.args("--test", testOutput.getOutputFile().getAbsolutePath());

        if (ext.getBaseOutputDir().isPresent()) {
          File outputsDir = ext.getBaseOutputDir().map(dir -> dir.dir(variant.getName())).get().getAsFile();
          task.args("--outputs-dir", outputsDir.getAbsolutePath());
        }

        if (ext.getDevices().isPresent() && !ext.getDevices().get().isEmpty()) {
          ext.getDevices().get().forEach(device ->
            task.args("--device", device.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue().toString())
                .collect(Collectors.joining(",")))
          );
        }

        if (Boolean.TRUE.equals(ext.getUseOrchestrator().getOrNull())) {
          task.args("--use-orchestrator");
        }

        if (Boolean.TRUE.equals(ext.getClearPackageData().getOrNull())) {
          task.args("--clear-package-data");
        }

        if (Boolean.TRUE.equals(ext.getWithCoverage().getOrNull())) {
          task.args("--with-coverage");
        }

        if (ext.getAdditionalApks().isPresent() && !ext.getAdditionalApks().get().isEmpty()) {
          task.args("--additional-apks", ext.getAdditionalApks().get().getFiles().stream()
              .map(File::getAbsolutePath)
              .collect(Collectors.joining(",")));
        }

        if (ext.getEnvironmentVariables().isPresent() && !ext.getEnvironmentVariables().get().isEmpty()) {
          String envLine = ext.getEnvironmentVariables().get().entrySet().stream()
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(Collectors.joining(","));
          task.args("--environment-variables", envLine);
        }

        if (ext.getUniformShards().isPresent()) {
          task.args("--num-uniform-shards", String.valueOf(ext.getUniformShards().get()));
        } else if (ext.getNumShards().isPresent()) {
          task.args("--num-shards", String.valueOf(ext.getNumShards().get()));
        }

        if (ext.getDirectoriesToPull().isPresent() && !ext.getDirectoriesToPull().get().isEmpty()) {
          task.args("--directories-to-pull", String.join(",", ext.getDirectoriesToPull().get()));
        }
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
