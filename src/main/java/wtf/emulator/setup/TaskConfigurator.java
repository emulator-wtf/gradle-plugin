package wtf.emulator.setup;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.TestedVariant;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskProvider;
import wtf.emulator.EwExecSummaryTask;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwVariantFilter;
import wtf.emulator.async.EwAsyncExecService;
import wtf.emulator.async.EwAsyncExecTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
  private final Configuration toolConfig;
  private final TaskProvider<EwExecSummaryTask> rootTask;
  private final SetProperty<String> failureCollector;
  private final Provider<EwAsyncExecService> service;

  public TaskConfigurator(Project target, EwExtension ext, Configuration toolConfig, TaskProvider<EwExecSummaryTask> rootTask, SetProperty<String> failureCollector, Provider<EwAsyncExecService> service) {
    this.target = target;
    this.ext = ext;
    this.toolConfig = toolConfig;
    this.rootTask = rootTask;
    this.failureCollector = failureCollector;
    this.service = service;
  }

  public <T extends TestedVariant & BaseVariant> void configureEwTask(
      BaseExtension android,
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
    File outputFile = intermediateFolder.resolve(variant.getName() + ".json").toFile();

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

    final TaskProvider<? extends EwExecTask> execTask;

    if (ext.getAsync().isPresent() && ext.getAsync().get()) {
      Consumer<EwAsyncExecTask> asyncAdditionalConfigure = (task) -> {
        task.getExecService().set(service);
        task.getOutputs().upToDateWhen((theTask) -> false);
        additionalConfigure.accept(task);
      };
      execTask = target.getTasks().register(taskName, EwAsyncExecTask.class, task ->
          configureTask(android, variant, asyncAdditionalConfigure, outputFile, outputFailureFile, task)
      );
    } else {
      execTask = target.getTasks().register(taskName, EwExecTask.class, task ->
          configureTask(android, variant, additionalConfigure, outputFile, outputFailureFile, task)
      );
    }

    rootTask.configure(task -> task.dependsOn(execTask));
  }

  private <VariantType extends TestedVariant & BaseVariant, TaskType extends EwExecTask> void configureTask(
      BaseExtension android,
      VariantType variant,
      Consumer<TaskType> additionalConfigure,
      File outputFile,
      File outputFailureFile,
      TaskType task
  ) {
    task.setDescription("Run " + variant.getName() + " instrumentation tests with emulator.wtf");
    task.setGroup("Verification");

    if (ext.getSideEffects().isPresent() && ext.getSideEffects().get()) {
      task.getOutputs().upToDateWhen((t) -> false);
      task.getSideEffects().set(true);
    }

    task.getOutputFile().set(outputFile);

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
    task.getShardTargetRuntime().set(ext.getShardTargetRuntime());

    task.getDirectoriesToPull().set(ext.getDirectoriesToPull());

    task.getTestTimeout().set(ext.getTimeout());

    task.getFileCacheEnabled().set(ext.getFileCacheEnabled());
    task.getFileCacheTtl().set(ext.getFileCacheTtl());

    task.getTestCacheEnabled().set(ext.getTestCacheEnabled());

    task.getNumFlakyTestAttempts().set(ext.getNumFlakyTestAttempts());
    task.getFlakyTestRepeatMode().set(ext.getFlakyTestRepeatMode());

    task.getScmUrl().set(ext.getScmUrl());
    task.getScmCommitHash().set(ext.getScmCommitHash());
    task.getScmRefName().set(ext.getScmRefName());
    task.getScmPrUrl().set(ext.getScmPrUrl());

    task.getPrintOutput().set(ext.getPrintOutput());

    task.getDisplayName().set(ext.getDisplayName().orElse(ext.getVariantCount().map((count) -> {
      String name = target.getPath();
      if (name.equals(":")) {
        // replace with rootProject name
        name = target.getName();
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

    task.getTestTargets().set(ext.getTestTargets());

    task.getProxyHost().set(ext.getProxyHost());
    task.getProxyPort().set(ext.getProxyPort());
    task.getProxyUser().set(ext.getProxyUser());
    task.getProxyPassword().set(ext.getProxyPassword());

    additionalConfigure.accept(task);
  }

  private static String capitalize(String str) {
    if (str.length() == 0) {
      return str;
    }
    return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
  }
}
