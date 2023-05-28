package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.api.GradleException;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.workers.WorkAction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.emulator.ext.Slf4jInfoOutputStream;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EwWorkAction implements WorkAction<EwWorkParameters> {
  @Inject
  public abstract ExecOperations getExecOperations();

  private final Logger log = LoggerFactory.getLogger("emulator.wtf");

  @Override
  public void execute() {
    // materialize token
    final String token = getParameters().getToken().getOrNull();
    if (token == null) {
      throw new IllegalArgumentException("Missing token for emulator.wtf.\n" +
          "Did you forget to set token in the emulatorwtf {} block or EW_API_TOKEN env var?");
    }

    try {
      final ExecOperations exec = getExecOperations();

      ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
      ByteArrayOutputStream errorOut = new ByteArrayOutputStream();

      ExecResult result = exec.javaexec(spec -> {
        // use env var for passing token so it doesn't get logged out with --info
        spec.environment("EW_API_TOKEN", token);

        spec.classpath(getParameters().getClasspath().get());

        if (getParameters().getWorkingDir().isPresent()) {
          spec.workingDir(getParameters().getWorkingDir().get());
        }

        spec.args("--ew-integration", "gradle-plugin " + BuildConfig.VERSION);

        if (getParameters().getDisplayName().isPresent()) {
          spec.args("--display-name", getParameters().getDisplayName().get());
        }

        if (getParameters().getScmUrl().isPresent()) {
          spec.args("--scm-url", getParameters().getScmUrl().get());
        }

        if (getParameters().getScmCommitHash().isPresent()) {
          spec.args("--scm-commit", getParameters().getScmCommitHash().get());
        }

        if (getParameters().getLibraryTestApk().isPresent()) {
          spec.args("--library-test", getParameters().getLibraryTestApk().get().getAsFile().getAbsolutePath());
        } else {
          spec.args("--app", getParameters().getAppApk().get().getAsFile().getAbsolutePath());
          spec.args("--test", getParameters().getTestApk().get().getAsFile().getAbsolutePath());
        }

        if (getParameters().getOutputsDir().isPresent()) {
          spec.args("--outputs-dir", getParameters().getOutputsDir().get().getAsFile().getAbsolutePath());
        }

        if (getParameters().getOutputs().isPresent() && !getParameters().getOutputs().get().isEmpty()) {
          String outputs = getParameters().getOutputs().get().stream().map(OutputType::getTypeName).collect(Collectors.joining(","));
          spec.args("--outputs", outputs);
        }

        if (getParameters().getRecordVideo().isPresent() && getParameters().getRecordVideo().get()) {
          spec.args("--record-video");
        }

        if (getParameters().getTimeout().isPresent()) {
          spec.args("--timeout", toCliString(getParameters().getTimeout().get()));
        }

        if (getParameters().getDevices().isPresent()) {
          getParameters().getDevices().get().forEach(device -> {
            if (!device.isEmpty()) {
              spec.args("--device", device.entrySet().stream()
                  .map(it -> it.getKey() + "=" + it.getValue())
                  .collect(Collectors.joining(","))
              );
            }
          });
        }

        if (Boolean.TRUE.equals(getParameters().getUseOrchestrator().getOrNull())) {
          spec.args("--use-orchestrator");
        }

        if (Boolean.TRUE.equals(getParameters().getClearPackageData().getOrNull())) {
          spec.args("--clear-package-data");
        }

        if (Boolean.TRUE.equals(getParameters().getWithCoverage().getOrNull())) {
          spec.args("--with-coverage");
        }

        if (getParameters().getAdditionalApks().isPresent()) {
          Set<File> additionalApks = getParameters().getAdditionalApks().get().getFiles();
          if (!additionalApks.isEmpty()) {
            spec.args("--additional-apks", additionalApks.stream()
                .map(File::getAbsolutePath).collect(Collectors.joining(",")));
          }
        }

        if (getParameters().getEnvironmentVariables().isPresent()) {
          Map<String, String> env = getParameters().getEnvironmentVariables().get();
          if (!env.isEmpty()) {
            String envLine = env.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
            spec.args("--environment-variables", envLine);
          }
        }

        if (getParameters().getNumBalancedShards().isPresent()) {
          spec.args("--num-balanced-shards", String.valueOf(getParameters().getNumBalancedShards().get()));
        } else if (getParameters().getNumUniformShards().isPresent()) {
          spec.args("--num-uniform-shards", String.valueOf(getParameters().getNumUniformShards().get()));
        } else if (getParameters().getNumShards().isPresent()) {
          spec.args("--num-shards", String.valueOf(getParameters().getNumShards().get()));
        }

        if (getParameters().getDirectoriesToPull().isPresent()) {
          List<String> dirsToPull = getParameters().getDirectoriesToPull().get();
          if (!dirsToPull.isEmpty()) {
            spec.args("--directories-to-pull", String.join(",", dirsToPull));
          }
        }

        if (getParameters().getSideEffects().isPresent() && getParameters().getSideEffects().get()) {
          spec.args("--side-effects");
        }

        if (getParameters().getFileCacheEnabled().isPresent() && !getParameters().getFileCacheEnabled().get()) {
          spec.args("--no-file-cache");
        } else if (getParameters().getFileCacheTtl().isPresent()) {
          spec.args("--file-cache-ttl", toCliString(getParameters().getFileCacheTtl().get()));
        }

        if (getParameters().getTestCacheEnabled().isPresent() && !getParameters().getTestCacheEnabled().get()) {
          spec.args("--no-test-cache");
        }

        if (getParameters().getNumFlakyTestAttempts().isPresent()) {
          spec.args("--num-flaky-test-attempts", getParameters().getNumFlakyTestAttempts().get().toString());
        }

        if (getParameters().getAsync().getOrElse(false)) {
          spec.args("--async");
        }

        if (getParameters().getTestTargets().isPresent()) {
          spec.args("--test-targets", getParameters().getTestTargets().get());
        }

        if (getParameters().getProxyHost().isPresent()) {
          spec.args("--proxy-host", getParameters().getProxyHost().get());
        }
        if (getParameters().getProxyPort().isPresent()) {
          spec.args("--proxy-port", getParameters().getProxyPort().get().toString());
        }
        if (getParameters().getProxyUser().isPresent()) {
          spec.args("--proxy-user", getParameters().getProxyUser().get());
        }
        if (getParameters().getProxyPassword().isPresent()) {
          spec.args("--proxy-password", getParameters().getProxyPassword().get());
        }

        spec.args("--json");

        if (getParameters().getPrintOutput().getOrElse(false)) {
          // redirect forked proc stderr to stdout
          spec.setErrorOutput(System.out);
        } else {
          spec.setErrorOutput(new TeeOutputStream(errorOut, new Slf4jInfoOutputStream(log)));
        }
        spec.setStandardOutput(jsonOut);

        spec.setIgnoreExitValue(true);
      });

      if (!getParameters().getPrintOutput().getOrElse(false) && result.getExitValue() != 0) {
        // always print output even if it wasn't request in case of an error
        System.out.println(errorOut);
      }

      if (result.getExitValue() != 0) {
        // not relevant in our use-case
        JSONObject json = new JSONObject(jsonOut.toString());
        String resultsUrl = json.optString("resultsUrl");
        String error = json.optString("error");

        final String message;
        if (error != null && error.length() > 0) {
          message = "emulator.wtf test run failed: " + error;
        } else {
          if (resultsUrl != null && resultsUrl.length() > 0) {
            message = "emulator.wtf test run failed. Details: " + resultsUrl;
          } else {
            message = "emulator.wtf test run failed";
          }
        }

        if (getParameters().getIgnoreFailures().getOrElse(false)) {
          log.warn(message);
          // if output failure file was given, write message there
          if (getParameters().getOutputFailureFile().isPresent()) {
            File failureFile = getParameters().getOutputFailureFile().get().getAsFile();
            try {
              StringBuilder fileMessage = new StringBuilder();
              if (getParameters().getDisplayName().isPresent()) {
                fileMessage.append(getParameters().getDisplayName().get()).append(": ");
                fileMessage.append(message);
              }
              FileUtils.write(failureFile, fileMessage.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
              /* ignore */
            }
          }
        } else {
          throw new GradleException(message);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String toCliString(Duration duration) {
    return duration.getSeconds() + "s";
  }
}
