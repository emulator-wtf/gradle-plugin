package wtf.emulator.exec;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;
import wtf.emulator.OutputType;

import java.io.File;

public interface EwCollectResultsWorkParameters extends WorkParameters  {
  SetProperty<File> getClasspath();

  DirectoryProperty getOutputsDir();

  ListProperty<OutputType> getOutputs();

  Property<Boolean> getPrintOutput();

  Property<String> getProxyHost();

  Property<Integer> getProxyPort();

  Property<String> getProxyUser();

  Property<String> getProxyPassword();
}
