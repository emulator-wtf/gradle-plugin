package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.exec.EwCliOutput;
import wtf.emulator.junit.JUnitResults;
import wtf.emulator.junit.JUnitXmlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class EwReportTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract RegularFileProperty getCliOutputFile(); // intermediate json

  @InputDirectory
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract DirectoryProperty getOutputDir();

  @InputFile
  @NotNull Provider<RegularFile> getMergedXml() {
    return getOutputDir().file("results.xml");
  }

  @TaskAction
  public void exec() {
    JUnitResults jUnitResults;
    try {
      jUnitResults = JUnitXmlParser.parseJUnitXml(new FileInputStream(getMergedXml().get().getAsFile()));
    } catch (FileNotFoundException e) {
      getLogger().warn("Junit XML not found: {}", getMergedXml().get().getAsFile().getAbsolutePath());
      return;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    EwCliOutput cliOutput = readOutput(getCliOutputFile().getAsFile().get());
    CliOutputSync sync = cliOutput.sync();
    String resultsUrl = null;
    if (sync != null) {
      resultsUrl = sync.resultsUrl();
    }
    GradleCompatFactory.get(getProject().getGradle()).reportTestResults(getProject(), jUnitResults, resultsUrl);
  }

  private static EwCliOutput readOutput(File file) {
    try {
      return EwJson.gson.fromJson(FileUtils.readFileToString(file, "UTF-8"), EwCliOutput.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
