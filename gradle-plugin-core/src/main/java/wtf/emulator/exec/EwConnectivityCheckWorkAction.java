package wtf.emulator.exec;

import org.gradle.api.file.FileSystemOperations;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import wtf.emulator.EwJson;

import javax.inject.Inject;

public abstract class EwConnectivityCheckWorkAction implements WorkAction<EwConnectivityCheckWorkParameters> {
  @Inject
  public abstract ExecOperations getExecOperations();

  @Inject
  public abstract FileSystemOperations getFileSystemOperations();

  @Override
  public void execute() {
    EwConnectivityCheckWorkParameters parameters = getParameters();
    EwCliExecutor cliExecutor = new EwCliExecutor(EwJson.gson, getExecOperations());
    cliExecutor.connectivityCheck(parameters);
  }
}
