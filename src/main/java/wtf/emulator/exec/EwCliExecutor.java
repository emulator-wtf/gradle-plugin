package wtf.emulator.exec;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.emulator.BuildConfig;
import wtf.emulator.OutputType;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.ext.Slf4jInfoOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EwCliExecutor {
  private final Logger log = LoggerFactory.getLogger("emulator.wtf");

  private final Gson gson;
  private final ExecOperations execOperations;

  public EwCliExecutor(Gson gson, ExecOperations execOperations) {
    this.gson = gson;
    this.execOperations = execOperations;
  }

  public String collectRunResults(EwCollectResultsWorkParameters parameters, String runUuid, String runToken, String startTime, String displayName, String folderName) {
    // materialize token
    if (runToken == null) {
      throw new IllegalArgumentException("Missing token for collecting emulator.wtf results. This is probably a bug - let us know at support@emulator.wtf.");
    }

    try {
      ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
      ByteArrayOutputStream errorOut = new ByteArrayOutputStream();

      ExecResult result = execOperations.javaexec(spec -> {

        Directory baseOutputDir = parameters.getOutputsDir().getOrNull();
        final File outputsDir;

        if (baseOutputDir == null) {
          outputsDir = null;
        } else {
          outputsDir = new File(baseOutputDir.getAsFile(), folderName);
        }

        configureCollectExec(spec, parameters, runUuid, runToken, startTime, outputsDir);
        if (parameters.getPrintOutput().getOrElse(false)) {
          // redirect forked proc stderr to stdout
          spec.setErrorOutput(System.out);
        } else {
          spec.setErrorOutput(new TeeOutputStream(errorOut, new Slf4jInfoOutputStream(log)));
        }
        spec.setStandardOutput(jsonOut);

        spec.setIgnoreExitValue(true);
      });

      if (!parameters.getPrintOutput().getOrElse(false) && result.getExitValue() != 0) {
        // always print output even if it wasn't requested in case of an error
        System.out.println(errorOut);
      }

      JSONObject json = new JSONObject(jsonOut.toString());
      String resultsUrl = json.optString("resultsUrl");

      if (result.getExitValue() != 0) {
        String error = json.optString("error");

        final String message;
        if (error != null && error.length() > 0) {
          message = "❌ " + displayName + " tests failed: " + error;
        } else {
          if (resultsUrl != null && resultsUrl.length() > 0) {
            message = "❌ " + displayName + " tests failed.\nDetails: " + resultsUrl + "\n";
          } else {
            message = "❌ " + displayName + " tests failed";
          }
        }
        return message;
      } else {
        if (resultsUrl != null && resultsUrl.length() > 0) {
          return "✅ " + displayName + " tests passed.\nDetails: " + resultsUrl + "\n";
        } else {
          return "✅ " + displayName + " tests passed";
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public EwCliOutput invokeCli(EwWorkParameters parameters) {
    // materialize token
    final String token = parameters.getToken().getOrNull();
    if (token == null) {
      throw new IllegalArgumentException("Missing token for emulator.wtf.\n" +
          "Did you forget to set token in the emulatorwtf {} block or EW_API_TOKEN env var?");
    }

    try {
      ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
      ByteArrayOutputStream errorOut = new ByteArrayOutputStream();

      ExecResult result = execOperations.javaexec(spec -> {
        configureCliExec(spec, parameters, token);
        if (parameters.getPrintOutput().getOrElse(false)) {
          // redirect forked proc stderr to stdout
          spec.setErrorOutput(System.out);
        } else {
          spec.setErrorOutput(new TeeOutputStream(errorOut, new Slf4jInfoOutputStream(log)));
        }
        spec.setStandardOutput(jsonOut);

        spec.setIgnoreExitValue(true);
      });

      if (!parameters.getPrintOutput().getOrElse(false) && result.getExitValue() != 0) {
        // always print output even if it wasn't requested in case of an error
        System.out.println(errorOut);
      }

      final EwCliOutput output;
      if (parameters.getAsync().getOrElse(false)) {
        output = EwCliOutput.create(gson.fromJson(jsonOut.toString(), CliOutputAsync.class));
      } else {
        output = EwCliOutput.create(gson.fromJson(jsonOut.toString(), CliOutputSync.class));
      }

      // write output json to the intermediate file
      if (parameters.getOutputFile().isPresent()) {
        File outputFile = parameters.getOutputFile().get().getAsFile();
        try {
          FileUtils.write(outputFile, gson.toJson(output), StandardCharsets.UTF_8);
        } catch (IOException e) {
          /* ignore */
        }
      }

      if (result.getExitValue() != 0 && output.sync() != null) {
        final String message = getFailureMessage(output.sync());

        if (parameters.getIgnoreFailures().getOrElse(false)) {
          log.warn(message);
          // if output failure file was given, write message there
          if (parameters.getOutputFailureFile().isPresent()) {
            File failureFile = parameters.getOutputFailureFile().get().getAsFile();
            try {
              StringBuilder fileMessage = new StringBuilder();
              if (parameters.getDisplayName().isPresent()) {
                fileMessage.append(parameters.getDisplayName().get()).append(": ");
                fileMessage.append(message);
              }
              FileUtils.write(failureFile, fileMessage.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
              /* ignore */
            }
          }
          return output;
        } else {
          throw new GradleException(message);
        }
      } else {
        return output;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static @NotNull String getFailureMessage(CliOutputSync output) {
    String resultsUrl = output.resultsUrl();
    String error = output.runResultsSummary() != null ? output.runResultsSummary().error() : null;

    final String message;
    if (error != null && !error.isEmpty()) {
      message = "emulator.wtf test run failed: " + error;
    } else {
      if (resultsUrl != null && !resultsUrl.isEmpty()) {
        message = "emulator.wtf test run failed. Details: " + resultsUrl;
      } else {
        message = "emulator.wtf test run failed";
      }
    }
    return message;
  }

  protected static void configureCollectExec(JavaExecSpec spec, EwCollectResultsWorkParameters parameters,
                                             String runUuid, String runToken, String startTime, @Nullable File outputFolder) {
    // use env var for passing token so it doesn't get logged out with --info
    spec.environment("EW_RUN_TOKEN", runToken);

    spec.classpath(parameters.getClasspath().get());

    spec.args("--collect-results");

    spec.args("--run-uuid", runUuid);
    spec.args("--start-time", startTime);

    if (outputFolder != null) {
      spec.args("--outputs-dir", outputFolder.getAbsolutePath());
    }

    if (outputFolder != null && parameters.getOutputs().isPresent() && !parameters.getOutputs().get().isEmpty()) {
      String outputs = parameters.getOutputs().get().stream().map(OutputType::getTypeName).collect(Collectors.joining(","));
      spec.args("--outputs", outputs);
    }

    if (parameters.getProxyHost().isPresent()) {
      spec.args("--proxy-host", parameters.getProxyHost().get());
    }
    if (parameters.getProxyPort().isPresent()) {
      spec.args("--proxy-port", parameters.getProxyPort().get().toString());
    }
    if (parameters.getProxyUser().isPresent()) {
      spec.args("--proxy-user", parameters.getProxyUser().get());
    }
    if (parameters.getProxyPassword().isPresent()) {
      spec.args("--proxy-password", parameters.getProxyPassword().get());
    }

    spec.args("--json");
  }

  protected static void configureCliExec(JavaExecSpec spec, EwWorkParameters parameters, String token) {
    // use env var for passing token so it doesn't get logged out with --info
    spec.environment("EW_API_TOKEN", token);

    spec.classpath(parameters.getClasspath().get());

    if (parameters.getWorkingDir().isPresent()) {
      spec.workingDir(parameters.getWorkingDir().get());
    }

    spec.args("--ew-integration", "gradle-plugin " + BuildConfig.VERSION);

    if (parameters.getDisplayName().isPresent()) {
      spec.args("--display-name", parameters.getDisplayName().get());
    }

    if (parameters.getScmUrl().isPresent()) {
      spec.args("--scm-url", parameters.getScmUrl().get());
    }

    if (parameters.getScmCommitHash().isPresent()) {
      spec.args("--scm-commit", parameters.getScmCommitHash().get());
    }

    if (parameters.getScmRefName().isPresent()) {
      spec.args("--scm-ref-name", parameters.getScmRefName().get());
    }

    if (parameters.getScmPrUrl().isPresent()) {
      spec.args("--scm-pr-url", parameters.getScmPrUrl().get());
    }

    if (parameters.getLibraryTestApk().isPresent()) {
      spec.args("--library-test", parameters.getLibraryTestApk().get().getAsFile().getAbsolutePath());
    } else {
      spec.args("--app", parameters.getAppApk().get().getAsFile().getAbsolutePath());
      spec.args("--test", parameters.getTestApk().get().getAsFile().getAbsolutePath());
    }

    if (parameters.getOutputsDir().isPresent() && !(parameters.getAsync().getOrElse(false))) {
      spec.args("--outputs-dir", parameters.getOutputsDir().get().getAsFile().getAbsolutePath());
    }

    if (parameters.getOutputs().isPresent() && !parameters.getOutputs().get().isEmpty()) {
      String outputs = parameters.getOutputs().get().stream().map(OutputType::getTypeName).collect(Collectors.joining(","));
      spec.args("--outputs", outputs);
    }

    if (parameters.getRecordVideo().isPresent() && parameters.getRecordVideo().get()) {
      spec.args("--record-video");
    }

    if (parameters.getTimeout().isPresent()) {
      spec.args("--timeout", toCliString(parameters.getTimeout().get()));
    }

    if (parameters.getDevices().isPresent()) {
      parameters.getDevices().get().forEach(device -> {
        if (!device.isEmpty()) {
          spec.args("--device", device.entrySet().stream()
              .map(it -> it.getKey() + "=" + it.getValue())
              .collect(Collectors.joining(","))
          );
        }
      });
    }

    if (Boolean.TRUE.equals(parameters.getUseOrchestrator().getOrNull())) {
      spec.args("--use-orchestrator");
    }

    if (Boolean.TRUE.equals(parameters.getClearPackageData().getOrNull())) {
      spec.args("--clear-package-data");
    }

    if (Boolean.TRUE.equals(parameters.getWithCoverage().getOrNull())) {
      spec.args("--with-coverage");
    }

    if (parameters.getAdditionalApks().isPresent()) {
      Set<File> additionalApks = parameters.getAdditionalApks().get().getFiles();
      if (!additionalApks.isEmpty()) {
        spec.args("--additional-apks", additionalApks.stream()
            .map(File::getAbsolutePath).collect(Collectors.joining(",")));
      }
    }

    if (parameters.getEnvironmentVariables().isPresent()) {
      Map<String, String> env = parameters.getEnvironmentVariables().get();
      if (!env.isEmpty()) {
        String envLine = env.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(","));
        spec.args("--environment-variables", envLine);
      }
    }

    if (parameters.getShardTargetRuntime().isPresent()) {
      spec.args("--shard-target-runtime", parameters.getShardTargetRuntime().get() + "m");
    } else if (parameters.getNumBalancedShards().isPresent()) {
      spec.args("--num-balanced-shards", String.valueOf(parameters.getNumBalancedShards().get()));
    } else if (parameters.getNumUniformShards().isPresent()) {
      spec.args("--num-uniform-shards", String.valueOf(parameters.getNumUniformShards().get()));
    } else if (parameters.getNumShards().isPresent()) {
      spec.args("--num-shards", String.valueOf(parameters.getNumShards().get()));
    }

    if (parameters.getDirectoriesToPull().isPresent()) {
      List<String> dirsToPull = parameters.getDirectoriesToPull().get();
      if (!dirsToPull.isEmpty()) {
        spec.args("--directories-to-pull", String.join(",", dirsToPull));
      }
    }

    if (parameters.getSideEffects().isPresent() && parameters.getSideEffects().get()) {
      spec.args("--side-effects");
    }

    if (parameters.getFileCacheEnabled().isPresent() && !parameters.getFileCacheEnabled().get()) {
      spec.args("--no-file-cache");
    } else if (parameters.getFileCacheTtl().isPresent()) {
      spec.args("--file-cache-ttl", toCliString(parameters.getFileCacheTtl().get()));
    }

    if (parameters.getTestCacheEnabled().isPresent() && !parameters.getTestCacheEnabled().get()) {
      spec.args("--no-test-cache");
    }

    if (parameters.getNumFlakyTestAttempts().isPresent()) {
      spec.args("--num-flaky-test-attempts", parameters.getNumFlakyTestAttempts().get().toString());
    }

    if (parameters.getFlakyTestRepeatMode().isPresent()) {
      spec.args("--flaky-test-repeat-mode", parameters.getFlakyTestRepeatMode().get());
    }

    if (parameters.getAsync().getOrElse(false)) {
      spec.args("--async");
    }

    if (parameters.getTestTargets().isPresent()) {
      spec.args("--test-targets", parameters.getTestTargets().get());
    }

    if (parameters.getProxyHost().isPresent()) {
      spec.args("--proxy-host", parameters.getProxyHost().get());
    }
    if (parameters.getProxyPort().isPresent()) {
      spec.args("--proxy-port", parameters.getProxyPort().get().toString());
    }
    if (parameters.getProxyUser().isPresent()) {
      spec.args("--proxy-user", parameters.getProxyUser().get());
    }
    if (parameters.getProxyPassword().isPresent()) {
      spec.args("--proxy-password", parameters.getProxyPassword().get());
    }

    spec.args("--json");
  }

  private static String toCliString(Duration duration) {
    return duration.getSeconds() + "s";
  }
}
