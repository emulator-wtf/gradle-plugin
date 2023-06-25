package wtf.emulator.exec;

import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class EwWorkAction implements WorkAction<EwWorkParameters> {
  @Inject
  public abstract ExecOperations getExecOperations();

  private final Logger log = LoggerFactory.getLogger("emulator.wtf");

  @Override
  public void execute() {
    EwCliExecutor cliExecutor = new EwCliExecutor(getExecOperations());
    cliExecutor.invokeCli(getParameters());
  }
}
