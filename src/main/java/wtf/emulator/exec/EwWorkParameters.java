package wtf.emulator.exec;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;
import wtf.emulator.EwInvokeConfiguration;

import java.io.File;
import java.util.Map;

public interface EwWorkParameters extends EwInvokeConfiguration, WorkParameters {
  Property<String> getToken();

  RegularFileProperty getWorkingDir();

  SetProperty<File> getClasspath();

  RegularFileProperty getAppApk();

  RegularFileProperty getTestApk();

  RegularFileProperty getLibraryTestApk();

  DirectoryProperty getOutputsDir();

  ListProperty<Map<String, String>> getDevices();

  MapProperty<String, String> getEnvironmentVariables();

  RegularFileProperty getOutputFailureFile();
}
