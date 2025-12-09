package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.exec.CliOutputPrinter;
import wtf.emulator.exec.EwCliOutput;
import wtf.emulator.exec.EwCollectResultsWorkAction;
import wtf.emulator.exec.EwCollectResultsWorkParameters;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class EwExecSummaryTask extends DefaultTask {
  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract Property<FileCollection> getInputSummaryFiles();

  @Input
  @Optional
  public abstract Property<PrintMode> getPrintMode();

  @Input
  @Optional
  public abstract Property<Boolean> getWaitForAsync();

  @Classpath
  @InputFiles
  public abstract Property<FileCollection> getClasspath();

  @Internal
  public abstract DirectoryProperty getIntermediateOutputsDir();

  @Optional
  @OutputDirectory
  public abstract DirectoryProperty getOutputsDir();

  @Optional
  @Input
  public abstract ListProperty<OutputType> getOutputTypes();

  @Optional
  @Input
  public abstract Property<Boolean> getPrintOutput();

  @Optional
  @Input
  public abstract Property<Boolean> getDebug();

  @Optional
  @Input
  public abstract Property<String> getProxyHost();

  @Optional
  @Input
  public abstract Property<Integer> getProxyPort();

  @Optional
  @Input
  public abstract Property<String> getProxyUser();

  @Optional
  @Input
  public abstract Property<String> getProxyPassword();

  @Optional
  @Input
  public abstract ListProperty<String> getNonProxyHosts();

  @Inject
  public abstract WorkerExecutor getWorkerExecutor();

  @TaskAction
  public void exec() {
    final List<EwCliOutput> immaterializedOuts = getInputSummaryFiles().get().getFiles().stream()
        .map(EwExecSummaryTask::readOutput)
        .collect(Collectors.toList());

    final List<EwCliOutput> outs;

    if (getWaitForAsync().getOrElse(false)) {
      // wait for async executions to finish and map them to sync outputs
      outs = new ArrayList<>(immaterializedOuts.size());
      outs.addAll(filterOuts(immaterializedOuts, out -> out.async() == null));
      outs.addAll(waitForAsync(filterOuts(immaterializedOuts, out -> out.async() != null)));
    } else {
      outs = immaterializedOuts;
    }

    CliOutputPrinter printer = new CliOutputPrinter();

    if (getPrintMode().getOrElse(PrintMode.FAILURES_ONLY) != PrintMode.NONE) {
      if (getPrintMode().getOrElse(PrintMode.FAILURES_ONLY) == PrintMode.ALL) {
        final List<EwCliOutput> successOuts = filterOuts(outs, EwCliOutput::isSuccess);

        getLogger().lifecycle("emulator.wtf test results:");
        for (EwCliOutput out : successOuts) {
          getLogger().lifecycle(printer.getSummaryLines(out));
        }
      }

      final List<EwCliOutput> failureOuts = filterOuts(outs, EwCliOutput::isFailure);
      if (!failureOuts.isEmpty()) {
        getLogger().warn("There were emulator.wtf test failures:");
        for (EwCliOutput out : failureOuts) {
          getLogger().warn(printer.getSummaryLines(out));
        }
      }
    }
  }

  public List<EwCliOutput> waitForAsync(List<EwCliOutput> inputAsyncSummaries) {
    WorkQueue workQueue = getWorkerExecutor().noIsolation();

    List<AsyncInvoke> asyncInvokes = new ArrayList<>(inputAsyncSummaries.size());
    inputAsyncSummaries.forEach(out -> {
      CliOutputAsync async = out.async();
      if (async != null) {
        String asyncTaskId = getAsyncTaskId(out);
        File outputFile = new File(getIntermediateOutputsDir().get().getAsFile(), asyncTaskId + ".json");
        File outputDir = new File(getOutputsDir().get().getAsFile(), asyncTaskId);
        asyncInvokes.add(new AsyncInvoke(async, outputFile, outputDir, out.displayName(), out.taskPath()));
      }
    });

    asyncInvokes.forEach(invoke -> workQueue.submit(EwCollectResultsWorkAction.class, params ->
        fillCollectWorkParameters(params, invoke.async, invoke.displayName, invoke.taskPath, invoke.outputFile, invoke.outputDir)));

    workQueue.await();

    // read the outputs
    return asyncInvokes.stream().map(invoke -> readOutput(invoke.outputFile)).collect(Collectors.toList());
  }

  private static class AsyncInvoke {
    private final CliOutputAsync async;
    private final File outputFile;
    private final File outputDir;
    private final String displayName;
    private final String taskPath;

    private AsyncInvoke(CliOutputAsync async, File outputFile, File outputDir, String displayName, String taskPath) {
      this.async = async;
      this.outputFile = outputFile;
      this.outputDir = outputDir;
      this.displayName = displayName;
      this.taskPath = taskPath;

    }
  }

  private static String getAsyncTaskId(EwCliOutput out) {
    String sanitizedPath = out.taskPath().replace(":", "_");
    if (sanitizedPath.startsWith("_")) {
      return sanitizedPath.substring(1);
    }
    return sanitizedPath;
  }

  protected void fillCollectWorkParameters(EwCollectResultsWorkParameters p, CliOutputAsync async, String displayName, String taskPath, File summaryOut, File outputDir) {
    p.getClasspath().set(getClasspath());
    p.getOutputsDir().set(outputDir);
    p.getOutputFile().set(summaryOut);
    p.getOutputs().set(getOutputTypes());
    p.getPrintOutput().set(getPrintOutput());
    p.getDebug().set(getDebug());
    p.getProxyHost().set(getProxyHost());
    p.getProxyPort().set(getProxyPort());
    p.getProxyUser().set(getProxyUser());
    p.getProxyPassword().set(getProxyPassword());
    p.getNonProxyHosts().set(getNonProxyHosts());
    p.getRunUuid().set(async.runUuid());
    p.getRunToken().set(async.runToken());
    p.getDisplayName().set(displayName);
    p.getStartTime().set(async.startTime());
    p.getTaskPath().set(taskPath);
  }

  private static EwCliOutput readOutput(File file) {
    try {
      return EwJson.gson.fromJson(FileUtils.readFileToString(file, "UTF-8"), EwCliOutput.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<EwCliOutput> filterOuts(List<EwCliOutput> outs, Function<EwCliOutput, Boolean> filter) {
    return outs.stream().filter(filter::apply).collect(Collectors.toList());
  }
}
