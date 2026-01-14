package wtf.emulator.setup;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import wtf.emulator.DevelocityReporter;
import wtf.emulator.DslInternals;
import wtf.emulator.EwInvokeDsl;
import wtf.emulator.EwConnectivityCheckTask;
import wtf.emulator.EwExecSummaryTask;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwExtensionInternal;
import wtf.emulator.EwProperties;
import wtf.emulator.EwReportTask;
import wtf.emulator.PrintMode;
import wtf.emulator.TestReporter;
import wtf.emulator.attributes.EwArtifactType;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static wtf.emulator.setup.StringUtils.capitalize;

public class TaskConfigurator {
  private final Project target;
  private final EwExtension ext;
  private final EwExtensionInternal extInternals;
  private final Provider<Configuration> toolConfig;
  private final Configuration resultsExportConfig;
  private final Provider<Configuration> resultsImportConfig;

  private static final String ROOT_TASK_NAME = "testWithEmulatorWtf";
  private static final String CONNECTIVITY_CHECK_TASK_NAME = "emulatorWtfConnectivityCheck";

  public TaskConfigurator(Project target, EwExtension ext, EwExtensionInternal extInternals, Provider<Configuration> toolConfig, Configuration resultsExportConfig, Provider<Configuration> resultsImportConfig) {
    this.target = target;
    this.ext = ext;
    this.extInternals = extInternals;
    this.toolConfig = toolConfig;
    this.resultsExportConfig = resultsExportConfig;
    this.resultsImportConfig = resultsImportConfig;
  }

  public void configureRootTask() {
    // create root anchor task
    target.getTasks().register(ROOT_TASK_NAME, EwExecSummaryTask.class, task -> {
      task.setDescription("Run all instrumentation tests of all variants with emulator.wtf");
      task.setGroup("Verification");

      task.getPrintMode().set(PrintMode.ALL);
      task.getInputSummaryFiles().set(resultsImportConfig.map(importConfig ->
        resultsExportConfig.getOutgoing().getArtifacts().getFiles().plus(
          importConfig.getIncoming().artifactView((view) -> {
            view.getAttributes().attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
          }).getFiles()
        )
      ));

      task.getWaitForAsync().set(true);

      // props necessary for collecting results
      task.getClasspath().set(toolConfig);
      task.getIntermediateOutputsDir().set(target.getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf").resolve("async").toFile());
      task.getOutputsDir().set(ext.getBaseOutputDir().dir(task.getName()));
      task.getOutputTypes().set(ext.getOutputs());
      task.getPrintOutput().set(ext.getPrintOutput());
      task.getDebug().set(EwProperties.DEBUG.getFlagProvider(target, false));
      task.getProxyHost().set(ext.getProxyHost());
      task.getProxyPort().set(ext.getProxyPort());
      task.getProxyUser().set(ext.getProxyUser());
      task.getProxyPassword().set(ext.getProxyPassword());
      task.getNonProxyHosts().set(ext.getNonProxyHosts());

      // root task is never up-to-date
      task.getOutputs().upToDateWhen(it -> false);
    });
  }

  public void configureConnectivityCheckTask() {
    target.getTasks().register(CONNECTIVITY_CHECK_TASK_NAME, EwConnectivityCheckTask.class, task -> {
      task.setDescription("Check connectivity to emulator.wtf service");
      task.setGroup("Verification");

      task.getClasspath().set(toolConfig);

      task.getToken().set(ext.getToken().orElse(target.provider(() ->
          System.getenv("EW_API_TOKEN"))));

      task.getProxyHost().set(ext.getProxyHost());
      task.getProxyPort().set(ext.getProxyPort());
      task.getProxyUser().set(ext.getProxyUser());
      task.getProxyPassword().set(ext.getProxyPassword());
      task.getNonProxyHosts().set(ext.getNonProxyHosts());

      task.getDnsServers().set(ext.getDnsServers());
      task.getDnsOverrides().set(ext.getDnsOverrides());
      task.getRelays().set(ext.getRelays());
      task.getEgressTunnel().set(ext.getEgressTunnel());
      task.getEgressLocalhostForwardIp().set(ext.getEgressLocalhostForwardIp());

      task.getVerbose().set(true);
      task.getDebug().set(EwProperties.DEBUG.getFlagProvider(target, false));
      task.getPrintOutput().set(true);

      // connectivity check is never up-to-date
      task.getOutputs().upToDateWhen(it -> false);
    });
  }

  public void configureEwTask(
    String ewInvokeName,
    EwInvokeDsl config,
    Consumer<EwExecTask> additionalTaskConfigure
  ) {
    // bump the variant count
    extInternals.getVariantCount().set(extInternals.getVariantCount().get() + 1);

    // create output file property for each variant
    Path intermediateFolder = target.getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf");
    File outputFile = intermediateFolder.resolve(ewInvokeName + ".json").toFile();

    // register the work task
    String taskName = "test" + capitalize(ewInvokeName) + "WithEmulatorWtf";
    final TaskProvider<? extends EwExecTask> execTask;
    Provider<Directory> outputDirectory = ext.getBaseOutputDir().dir(taskName);

    execTask = target.getTasks().register(taskName, EwExecTask.class, task ->
      configureTask(ewInvokeName, additionalTaskConfigure, outputFile, outputDirectory, task, config)
    );

    if (config.getTestReporters().isPresent() && !config.getAsync().getOrElse(false)) {
      Set<TestReporter> reporters = new HashSet<>(config.getTestReporters().get());
      for (TestReporter reporter : reporters) {
        switch (reporter) {
        case DEVELOCITY:
          DevelocityReporter.configure(target, execTask, EwExecTask::getMergedXmlDetached);
          break;
        case GRADLE_TEST_REPORTING_API:
          String reportTaskName = "report" + capitalize(ewInvokeName) + "EmulatorWtfTestResults";
          TaskProvider<? extends EwReportTask> reportTask = target.getTasks().register(reportTaskName, EwReportTask.class, task -> {
            task.getCliOutputFile().set(outputFile);
            task.getOutputDir().set(outputDirectory);
            task.getGradleVersion().set(target.getGradle().getGradleVersion());
          });
          execTask.configure(task -> task.finalizedBy(reportTask));
          break;
        }
      }
    }

    // register output file to results config
    resultsExportConfig.getOutgoing().artifact(outputFile, (it) -> it.builtBy(execTask));
  }

  private void configureTask(
      String variantName,
      Consumer<EwExecTask> additionalConfigure,
      File outputFile,
      Provider<Directory> outputDirectory,
      EwExecTask task,
      EwInvokeDsl config
  ) {
    task.setDescription("Run " + variantName + " instrumentation tests with emulator.wtf");
    task.setGroup("Verification");

    if (config.getSideEffects().isPresent() && config.getSideEffects().get()) {
      task.getOutputs().upToDateWhen((t) -> false);
      task.getSideEffects().set(true);
    }

    task.getAsync().set(config.getAsync());

    task.getOutputFile().set(outputFile);

    task.getClasspath().set(toolConfig);

    task.getToken().set(ext.getToken().orElse(target.provider(() ->
        System.getenv("EW_API_TOKEN"))));

    // don't configure outputs in async mode
    if (!task.getAsync().getOrElse(false)) {
      task.getOutputsDir().set(outputDirectory);
      task.getOutputTypes().set(config.getOutputs());
    }

    task.getRecordVideo().set(config.getRecordVideo());

    var devices = DslInternals.getDevices(config).stream().map(dev -> ProviderUtils.deviceToCliMap(target.getProviders(), dev)).toList();
    task.getDevices().set(ProviderUtils.reduce(
      target.provider(Collections::emptyList),
      devices,
      (acc, device) -> Stream.concat(acc.stream(), Stream.of(device)).toList()
    ));

    task.getUseOrchestrator().set(config.getUseOrchestrator());

    task.getClearPackageData().set(config.getClearPackageData());

    task.getWithCoverage().set(config.getWithCoverage());

    task.getAdditionalApks().set(config.getAdditionalApks());

    task.getInstrumentationRunner().set(config.getTestRunnerClass());

    task.getSecretEnvironmentVariables().set(config.getSecretEnvironmentVariables()
      .map((entries) -> {
        // toString the values
        final Map<String, String> out = new HashMap<>();
        entries.forEach((key, value) -> out.put(key, Objects.toString(value)));
        return out;
      }));

    task.getNumUniformShards().set(config.getNumUniformShards());
    task.getNumShards().set(config.getNumShards());
    task.getNumBalancedShards().set(config.getNumBalancedShards());
    task.getShardTargetRuntime().set(config.getShardTargetRuntime());

    task.getDirectoriesToPull().set(config.getDirectoriesToPull());

    task.getTestTimeout().set(config.getTimeout());

    task.getFileCacheEnabled().set(config.getFileCacheEnabled());
    task.getFileCacheTtl().set(config.getFileCacheTtl());

    task.getTestCacheEnabled().set(config.getTestCacheEnabled());
    if (config.getTestCacheEnabled().isPresent() && !config.getTestCacheEnabled().get()) {
      // if test cache is disabled, always rerun the task
      task.getOutputs().upToDateWhen(it -> false);
    }

    task.getNumFlakyTestAttempts().set(config.getNumFlakyTestAttempts());
    task.getFlakyTestRepeatMode().set(config.getFlakyTestRepeatMode());

    task.getScmUrl().set(config.getScmUrl());
    task.getScmCommitHash().set(config.getScmCommitHash());
    task.getScmRefName().set(config.getScmRefName());
    task.getScmPrUrl().set(config.getScmPrUrl());

    task.getDnsServers().set(config.getDnsServers());
    task.getDnsOverrides().set(config.getDnsOverrides());
    task.getRelays().set(config.getRelays());
    task.getEgressTunnel().set(config.getEgressTunnel());
    task.getEgressLocalhostForwardIp().set(config.getEgressLocalhostForwardIp());

    task.getPrintOutput().set(config.getPrintOutput());
    task.getDebug().set(EwProperties.DEBUG.getFlagProvider(target, false));

    task.getDisplayName().set(config.getDisplayName().orElse(extInternals.getVariantCount().map((count) -> {
      String name = target.getPath();
      if (name.equals(":")) {
        // replace with rootProject name
        name = target.getName();
      }
      if (count < 2) {
        return name;
      } else {
        return name + ":" + variantName;
      }
    })));

    task.getWorkingDir().set(target.getRootDir());

    task.getIgnoreFailures().set(config.getIgnoreFailures());

    task.getTestTargets().set(config.getTestTargets());

    task.getProxyHost().set(config.getProxyHost());
    task.getProxyPort().set(config.getProxyPort());
    task.getProxyUser().set(config.getProxyUser());
    task.getProxyPassword().set(config.getProxyPassword());
    task.getNonProxyHosts().set(config.getNonProxyHosts());

    additionalConfigure.accept(task);
  }
}
