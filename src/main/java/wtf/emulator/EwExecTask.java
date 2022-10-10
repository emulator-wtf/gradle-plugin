package wtf.emulator;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EwExecTask extends DefaultTask {
  @Classpath
  @InputFiles
  public abstract Property<FileCollection> getClasspath();

  @Input
  public abstract Property<String> getToken();

  @Optional
  @InputFile
  public abstract RegularFileProperty getAppApk();

  @Optional
  @InputFile
  public abstract RegularFileProperty getTestApk();

  @Optional
  @InputFile
  public abstract RegularFileProperty getLibraryTestApk();

  @Optional
  @OutputDirectory
  public abstract DirectoryProperty getOutputsDir();

  @Optional
  @Input
  public abstract ListProperty<Map<String, String>> getDevices();

  @Optional
  @Input
  public abstract Property<Boolean> getUseOrchestrator();

  @Optional
  @Input
  public abstract Property<Boolean> getClearPackageData();

  @Optional
  @Input
  public abstract Property<Boolean> getWithCoverage();

  @Optional
  @InputFiles
  public abstract Property<FileCollection> getAdditionalApks();

  @Optional
  @Input
  public abstract MapProperty<String, String> getEnvironmentVariables();

  @Optional
  @Input
  public abstract Property<Integer> getNumUniformShards();

  @Optional
  @Input
  public abstract Property<Integer> getNumShards();

  @Optional
  @Input
  public abstract ListProperty<String> getDirectoriesToPull();

  @Optional
  @Input
  public abstract Property<Boolean> getSideEffects();

  @OutputFiles
  public FileCollection getOutputFiles() {
    if (getOutputsDir().isPresent()) {
      return getOutputsDir().getAsFileTree();
    }
    return getProject().files();
  }

  @TaskAction
  public void runTests() {
    // materialize token
    final String token = getToken().getOrNull();
    if (token == null) {
      throw new IllegalArgumentException("Missing token for emulator.wtf.\n" +
          "Did you forget to set token in the emulatorwtf {} block or EW_API_TOKEN env var?");
    }

    ExecResult result = getProject().javaexec((spec) -> {
      // use env var for passing token so it doesn't get logged out with --info
      spec.environment("EW_API_TOKEN", token);

      spec.classpath(getClasspath().get());

      if (getLibraryTestApk().isPresent()) {
        spec.args("--library-test", getLibraryTestApk().get().getAsFile().getAbsolutePath());
      } else {
        spec.args("--app", getAppApk().get().getAsFile().getAbsolutePath());
        spec.args("--test", getTestApk().get().getAsFile().getAbsolutePath());
      }

      if (getOutputsDir().isPresent()) {
        spec.args("--outputs-dir", getOutputsDir().get().getAsFile().getAbsolutePath());
      }

      if (getDevices().isPresent()) {
        getDevices().get().forEach(device -> {
          if (!device.isEmpty()) {
            spec.args("--device", device.entrySet().stream()
              .map(it -> it.getKey() + "=" + it.getValue())
              .collect(Collectors.joining(","))
            );
          }
        });
      }

      if (Boolean.TRUE.equals(getUseOrchestrator().getOrNull())) {
        spec.args("--use-orchestrator");
      }

      if (Boolean.TRUE.equals(getClearPackageData().getOrNull())) {
        spec.args("--clear-package-data");
      }

      if (Boolean.TRUE.equals(getWithCoverage().getOrNull())) {
        spec.args("--with-coverage");
      }

      if (getAdditionalApks().isPresent()) {
        Set<File> additionalApks = getAdditionalApks().get().getFiles();
        if (!additionalApks.isEmpty()) {
          spec.args("--additional-apks", additionalApks.stream()
              .map(File::getAbsolutePath).collect(Collectors.joining(",")));
        }
      }

      if (getEnvironmentVariables().isPresent()) {
        Map<String, String> env = getEnvironmentVariables().get();
        if (!env.isEmpty()) {
          String envLine = env.entrySet().stream()
              .filter(entry -> entry.getValue() != null)
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(Collectors.joining(","));
          spec.args("--environment-variables", envLine);
        }
      }

      if (getNumUniformShards().isPresent()) {
        spec.args("--num-uniform-shards", String.valueOf(getNumUniformShards().get()));
      } else if (getNumShards().isPresent()) {
        spec.args("--num-shards", String.valueOf(getNumShards().get()));
      }

      if (getDirectoriesToPull().isPresent()) {
        List<String> dirsToPull = getDirectoriesToPull().get();
        if (!dirsToPull.isEmpty()) {
          spec.args("--directories-to-pull", String.join(",", dirsToPull));
        }
      }

      if (getSideEffects().isPresent() && getSideEffects().get()) {
        spec.args("--side-effects");
      }
    });

    result.assertNormalExitValue();
  }
}
