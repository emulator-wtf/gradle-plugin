package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.exec.EwCliOutput;
import wtf.emulator.junit.JUnitResults;
import wtf.emulator.junit.JUnitXmlParser;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class EwReportTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getCliOutputFile(); // intermediate json

  @InputDirectory
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract DirectoryProperty getOutputDir();

  @Internal
  public abstract Property<String> getGradleVersion();

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  Provider<RegularFile> getMergedXml() {
    return getOutputDir().file("results.xml");
  }

  @Inject
  public abstract ObjectFactory getObjects();

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
    GradleCompatFactory.get(getGradleVersion().get()).reportTestResults(getObjects(), jUnitResults, resultsUrl);
  }

  private static EwCliOutput readOutput(File file) {
    try {
      return EwJson.gson.fromJson(FileUtils.readFileToString(file, "UTF-8"), EwCliOutput.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
