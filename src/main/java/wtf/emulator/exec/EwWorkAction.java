package wtf.emulator.exec;

import org.gradle.api.file.FileSystemOperations;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;

public abstract class EwWorkAction implements WorkAction<EwWorkParameters> {
  @Inject
  public abstract ExecOperations getExecOperations();

  @Inject
  public abstract FileSystemOperations getFileSystemOperations();

  @Override
  public void execute() {
    EwWorkParameters parameters = getParameters();
    if (parameters.getOutputsDir().isPresent()) {
      getFileSystemOperations().delete((spec) -> spec.delete(parameters.getOutputsDir()));
    }

    EwCliExecutor cliExecutor = new EwCliExecutor(getExecOperations());
    cliExecutor.invokeCli(parameters);
  }
}
