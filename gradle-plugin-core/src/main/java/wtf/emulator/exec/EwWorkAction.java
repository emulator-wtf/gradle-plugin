package wtf.emulator.exec;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import wtf.emulator.EmulatorWtfException;
import wtf.emulator.EwJson;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    EwCliExecutor cliExecutor = new EwCliExecutor(EwJson.gson, getExecOperations());
    EwCliOutput output = cliExecutor.invokeCli(parameters);

    // write output json to the intermediate file
    if (parameters.getOutputFile().isPresent()) {
      File outputFile = parameters.getOutputFile().get().getAsFile();
      try {
        FileUtils.write(outputFile, EwJson.gson.toJson(output), StandardCharsets.UTF_8);
      } catch (IOException e) {
        /* ignore */
      }
    }
  }
}
