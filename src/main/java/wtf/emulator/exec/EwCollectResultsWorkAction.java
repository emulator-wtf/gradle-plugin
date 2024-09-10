package wtf.emulator.exec;

import org.gradle.api.file.FileSystemOperations;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import wtf.emulator.EwPlugin;

import javax.inject.Inject;

public abstract class EwCollectResultsWorkAction implements WorkAction<EwCollectResultsWorkParameters> {
  @Inject
  public abstract ExecOperations getExecOperations();

  @Inject
  public abstract FileSystemOperations getFileSystemOperations();

  @Override
  public void execute() {
    EwCollectResultsWorkParameters parameters = getParameters();
    if (parameters.getOutputsDir().isPresent()) {
      getFileSystemOperations().delete((spec) -> spec.delete(parameters.getOutputsDir()));
    }

    EwCliExecutor cliExecutor = new EwCliExecutor(EwPlugin.gson, getExecOperations());
    cliExecutor.collectRunResults(parameters);
  }
}
