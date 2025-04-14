package wtf.emulator.exec;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.emulator.BuildConfig;
import wtf.emulator.EmulatorWtfException;
import wtf.emulator.EwJson;
import wtf.emulator.OutputType;
import wtf.emulator.data.AgpOutputMetadata;
import wtf.emulator.data.AgpOutputMetadataVersion;
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

  public void collectRunResults(EwCollectResultsWorkParameters parameters) {
    try {
      ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
      ByteArrayOutputStream errorOut = new ByteArrayOutputStream();

      ExecResult result = execOperations.javaexec(spec -> {
        configureCollectExec(spec, parameters);
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

      CliOutputSync resultsOutput = gson.fromJson(jsonOut.toString(), CliOutputSync.class).toBuilder().timeMs(null).build();
      EwCliOutput.Builder outputBuilder = EwCliOutput.builder(resultsOutput);

      if (parameters.getDisplayName().isPresent()) {
        outputBuilder.displayName(parameters.getDisplayName().get());
      }

      final EwCliOutput output = outputBuilder.taskPath(parameters.getTaskPath().get()).exitCode(result.getExitValue()).build();
      // write output json to the intermediate file
      if (parameters.getOutputFile().isPresent()) {
        File outputFile = parameters.getOutputFile().get().getAsFile();
        try {
          FileUtils.write(outputFile, gson.toJson(output), StandardCharsets.UTF_8);
        } catch (IOException e) {
          /* ignore */
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void invokeCli(EwWorkParameters parameters) {
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

      final EwCliOutput.Builder outputBuilder;
      if (parameters.getAsync().getOrElse(false)) {
        outputBuilder = EwCliOutput.builder(gson.fromJson(jsonOut.toString(), CliOutputAsync.class));
      } else {
        outputBuilder = EwCliOutput.builder(gson.fromJson(jsonOut.toString(), CliOutputSync.class));
      }

      if (parameters.getDisplayName().isPresent()) {
        outputBuilder.displayName(parameters.getDisplayName().get());
      }

      final EwCliOutput output = outputBuilder.taskPath(parameters.getTaskPath().get()).exitCode(result.getExitValue()).build();

      // write output json to the intermediate file
      if (parameters.getOutputFile().isPresent()) {
        File outputFile = parameters.getOutputFile().get().getAsFile();
        try {
          FileUtils.write(outputFile, gson.toJson(output), StandardCharsets.UTF_8);
        } catch (IOException e) {
          /* ignore */
        }
      }

      if (result.getExitValue() != 0) {
        final String message = new CliOutputPrinter().getSummaryLines(output);

        if (parameters.getIgnoreFailures().getOrElse(false)) {
          log.warn(message);
        } else {
          throw new EmulatorWtfException(message);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected static void configureCollectExec(JavaExecSpec spec, EwCollectResultsWorkParameters parameters) {
    // materialize token
    if (!parameters.getRunToken().isPresent()) {
      throw new IllegalArgumentException("Missing token for collecting emulator.wtf results. This is probably a bug - let us know at support@emulator.wtf.");
    }

    String runToken = parameters.getRunToken().get();

    // use env var for passing token so it doesn't get logged out with --info
    spec.environment("EW_RUN_TOKEN", runToken);

    spec.classpath(parameters.getClasspath().get());

    spec.args("--collect-results");

    spec.args("--run-uuid", parameters.getRunUuid());

    if (parameters.getStartTime().isPresent()) {
      spec.args("--start-time", parameters.getStartTime().get());
    }

    File outputFolder = parameters.getOutputsDir().getAsFile().getOrNull();
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
      Set<File> files = parameters.getApks().get();
      if (files.size() > 1) {
        throw new RuntimeException("Unexpected number of app apks encountered: " + files.size());
      }
      File appApk = files.iterator().next();
      if (appApk.isDirectory()) {
        // read output-metadata.json to pick the best apk
        appApk = pickBestApk(appApk);
      }
      spec.args("--app", appApk.getAbsolutePath());

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

    if (parameters.getSecretEnvironmentVariables().isPresent()) {
      Map<String, String> secretEnv = parameters.getSecretEnvironmentVariables().get();
      if (!secretEnv.isEmpty()) {
        String secretEnvLine = secretEnv.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(","));
        spec.args("--secret-environment-variables", secretEnvLine);
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

    if (parameters.getDnsServers().isPresent()) {
      parameters.getDnsServers().get().stream().limit(4).forEach(addr -> {
        spec.args("--dns-server", addr);
      });
    }

    if (parameters.getEgressTunnel().isPresent()) {
      spec.args("--egress-tunnel");
    }

    if (parameters.getEgressLocalhostForwardIp().isPresent()) {
      spec.args("--egress-localhost-forward-ip", parameters.getEgressLocalhostForwardIp().get());
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

  private static File pickBestApk(File apkDirectory) {
    try {
      File metadataFile = new File(apkDirectory, "output-metadata.json");
      String metadataJson = FileUtils.readFileToString(metadataFile, StandardCharsets.UTF_8);

      // read and validate version first
      AgpOutputMetadataVersion outputMetaVersion = EwJson.gson.fromJson(metadataJson, AgpOutputMetadataVersion.class);
      if (outputMetaVersion.version() != 3) {
        throw new RuntimeException("Unsupported AGP output-metadata.json version: " + outputMetaVersion);
      }

      AgpOutputMetadata outputMetadata = EwJson.gson.fromJson(metadataJson, AgpOutputMetadata.class);

      if (!outputMetadata.artifactType().type().equals("APK")) {
        throw new IllegalArgumentException("Unexpected artifactType=" + outputMetadata.artifactType().type() + " in " + metadataFile.getAbsolutePath());
      }

      if (outputMetadata.elements().size() == 1) {
        return new File(apkDirectory, outputMetadata.elements().get(0).outputFile());
      }

      // if there are splits, prefer x86 split as they're faster to upload
      List<AgpOutputMetadata.Element> x86Splits = outputMetadata.elements().stream()
          .filter(it -> it.type().equals("ONE_OF_MANY") &&
            it.filters().stream().anyMatch(filter -> filter.filterType().equals("ABI") && filter.value().equals("x86")))
          .collect(Collectors.toList());

      if (x86Splits.size() == 1) {
        return new File(apkDirectory, x86Splits.get(0).outputFile());
      }

      // if there are no splits, look for universal
      List<AgpOutputMetadata.Element> universalSplits = outputMetadata.elements().stream()
          .filter(it -> it.type().equals("UNIVERSAL") && it.filters().isEmpty())
          .collect(Collectors.toList());

      if (universalSplits.size() == 1) {
        return new File(apkDirectory, universalSplits.get(0).outputFile());
      }

      throw new RuntimeException("Failed to pick best apk from " + apkDirectory.getAbsolutePath() + ". Please send the output-metadata.json file from that folder to support@emulator.wtf.");

    } catch (IOException ioe) {
      throw new RuntimeException("Failed to read output-metadata.json from " + apkDirectory.getAbsolutePath(), ioe);
    }
  }
}
