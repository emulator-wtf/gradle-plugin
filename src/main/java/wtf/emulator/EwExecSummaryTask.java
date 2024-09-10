package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import wtf.emulator.data.RunResult;
import wtf.emulator.exec.CliOutputPrinter;
import wtf.emulator.exec.EwCliOutput;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EwExecSummaryTask extends DefaultTask {
  @InputFiles
  public abstract SetProperty<File> getFailureMessages();

  @Input
  @Optional
  public abstract Property<PrintMode> getPrintMode();

  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract Property<FileCollection> getInputSummaryFiles();

  @TaskAction
  public void exec() {
    final List<EwCliOutput> outs = getInputSummaryFiles().get().getFiles().stream()
        .map((file) -> {
          try {
            return EwPlugin.gson.fromJson(FileUtils.readFileToString(file, "UTF-8"), EwCliOutput.class);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());


    //TODO(madis) wait for async executions to finish and map them to sync outputs?

    CliOutputPrinter printer = new CliOutputPrinter();

    if (getPrintMode().getOrElse(PrintMode.FAILURES_ONLY) != PrintMode.NONE) {
      if (getPrintMode().getOrElse(PrintMode.FAILURES_ONLY) == PrintMode.ALL) {
        final List<EwCliOutput> successOuts = outs.stream()
            .filter(out -> !isFailure(out))
            .collect(Collectors.toList());

        getLogger().lifecycle("emulator.wtf test results:");
        for (EwCliOutput out : successOuts) {
          getLogger().lifecycle(printer.getSummaryLines(out));
        }
      }

      final List<EwCliOutput> failureOuts = outs.stream()
          .filter(EwExecSummaryTask::isFailure)
          .collect(Collectors.toList());

      if (!failureOuts.isEmpty()) {
        getLogger().warn("There were emulator.wtf test failures:");
        for (EwCliOutput out : failureOuts) {
          getLogger().warn(printer.getSummaryLines(out));
        }
      }
    }
  }

  private static boolean isFailure(EwCliOutput output) {
    if (output.exitCode() != 0) {
      return true;
    }

    if (output.sync() == null) {
      return false;
    }

    if (output.sync().runResultsSummary() == null) {
      return false;
    }

    EnumSet<RunResult> successyResults = EnumSet.of(RunResult.SUCCESS, RunResult.FLAKY, RunResult.CANCELED);

    return !successyResults.contains(output.sync().runResultsSummary().runResult());
  }
}
