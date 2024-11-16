package wtf.emulator.setup;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.BaseVariant;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import wtf.emulator.EwExecSummaryTask;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwExtensionInternal;
import wtf.emulator.EwVariantFilter;
import wtf.emulator.PrintMode;
import wtf.emulator.attributes.EwArtifactType;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TaskConfigurator {
  private final Project target;
  private final EwExtension ext;
  private final EwExtensionInternal extInternals;
  private final Provider<Configuration> toolConfig;
  private final Configuration resultsExportConfig;
  private final Provider<Configuration> resultsImportConfig;

  private static final String ROOT_TASK_NAME = "testWithEmulatorWtf";

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
      task.getIntermediateOutputsDir().set(target.getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf").toFile());
      task.getOutputsDir().set(ext.getBaseOutputDir().dir(task.getName()));
      task.getOutputTypes().set(ext.getOutputs());
      task.getPrintOutput().set(ext.getPrintOutput());
      task.getProxyHost().set(ext.getProxyHost());
      task.getProxyPort().set(ext.getProxyPort());
      task.getProxyUser().set(ext.getProxyUser());
      task.getProxyPassword().set(ext.getProxyPassword());

      // root task is never up-to-date
      task.getOutputs().upToDateWhen(it -> false);
    });
  }

  public <T extends BaseVariant> void configureEwTask(
      BaseExtension android,
      T variant,
      Consumer<EwExecTask> additionalConfigure
  ) {
    Action<EwVariantFilter> filter = extInternals.getFilter();
    if (filter != null) {
      EwVariantFilter filterSpec = new EwVariantFilter(variant);
      filter.execute(filterSpec);
      if (!filterSpec.isEnabled()) {
        return;
      }
    }

    // bump the variant count
    extInternals.getVariantCount().set(extInternals.getVariantCount().get() + 1);

    // create output file property for each variant
    Path intermediateFolder = target.getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf");
    File outputFile = intermediateFolder.resolve(variant.getName() + ".json").toFile();

    // register the work task
    String taskName = "test" + capitalize(variant.getName()) + "WithEmulatorWtf";

    final TaskProvider<? extends EwExecTask> execTask;

    execTask = target.getTasks().register(taskName, EwExecTask.class, task ->
        configureTask(android, variant.getName(), variant.getBuildType().isTestCoverageEnabled(), variant.getMergedFlavor().getTestInstrumentationRunnerArguments(), additionalConfigure, outputFile, task)
    );

    // register output file to results config
    resultsExportConfig.getOutgoing().artifact(outputFile, (it) -> it.builtBy(execTask));
  }

  private void configureTask(
      BaseExtension android,
      String variantName,
      boolean testCoverageEnabled,
      Map<String, String> instrumentationRunnerArguments,
      Consumer<EwExecTask> additionalConfigure,
      File outputFile,
      EwExecTask task
  ) {
    task.setDescription("Run " + variantName + " instrumentation tests with emulator.wtf");
    task.setGroup("Verification");

    if (ext.getSideEffects().isPresent() && ext.getSideEffects().get()) {
      task.getOutputs().upToDateWhen((t) -> false);
      task.getSideEffects().set(true);
    }

    task.getAsync().set(ext.getAsync());

    task.getOutputFile().set(outputFile);

    task.getClasspath().set(toolConfig);

    task.getToken().set(ext.getToken().orElse(target.provider(() ->
        System.getenv("EW_API_TOKEN"))));

    // don't configure outputs in async mode
    if (!task.getAsync().getOrElse(false)) {
      task.getOutputsDir().set(ext.getBaseOutputDir().dir(task.getName()));
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

    task.getWithCoverage().set(ext.getWithCoverage().orElse(target.provider(() -> testCoverageEnabled)));

    task.getAdditionalApks().set(ext.getAdditionalApks());

    task.getEnvironmentVariables().set(ext.getEnvironmentVariables()
        .map((entries) -> {
          // pick defaults from test instrumentation runner args, then fill with overrides
          final Map<String, String> out = new HashMap<>(instrumentationRunnerArguments);
          entries.forEach((key, value) -> out.put(key, Objects.toString(value)));
          return out;
        }));

    task.getNumUniformShards().set(ext.getNumUniformShards());
    task.getNumShards().set(ext.getNumShards());
    task.getNumBalancedShards().set(ext.getNumBalancedShards());
    task.getShardTargetRuntime().set(ext.getShardTargetRuntime());

    task.getDirectoriesToPull().set(ext.getDirectoriesToPull());

    task.getTestTimeout().set(ext.getTimeout());

    task.getFileCacheEnabled().set(ext.getFileCacheEnabled());
    task.getFileCacheTtl().set(ext.getFileCacheTtl());

    task.getTestCacheEnabled().set(ext.getTestCacheEnabled());
    if (ext.getTestCacheEnabled().isPresent() && !ext.getTestCacheEnabled().get()) {
      // if test cache is disabled, always rerun the task
      task.getOutputs().upToDateWhen(it -> false);
    }

    task.getNumFlakyTestAttempts().set(ext.getNumFlakyTestAttempts());
    task.getFlakyTestRepeatMode().set(ext.getFlakyTestRepeatMode());

    task.getScmUrl().set(ext.getScmUrl());
    task.getScmCommitHash().set(ext.getScmCommitHash());
    task.getScmRefName().set(ext.getScmRefName());
    task.getScmPrUrl().set(ext.getScmPrUrl());

    task.getPrintOutput().set(ext.getPrintOutput());

    task.getDisplayName().set(ext.getDisplayName().orElse(extInternals.getVariantCount().map((count) -> {
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

    task.getIgnoreFailures().set(ext.getIgnoreFailures());

    task.getTestTargets().set(ext.getTestTargets());

    task.getProxyHost().set(ext.getProxyHost());
    task.getProxyPort().set(ext.getProxyPort());
    task.getProxyUser().set(ext.getProxyUser());
    task.getProxyPassword().set(ext.getProxyPassword());

    additionalConfigure.accept(task);
  }

  private static String capitalize(String str) {
    if (str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
  }
}
