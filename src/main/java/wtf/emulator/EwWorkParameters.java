package wtf.emulator;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;

import java.io.File;
import java.util.Map;

public interface EwWorkParameters extends EwInvokeConfiguration, WorkParameters {
  public abstract Property<String> getToken();

  public abstract SetProperty<File> getClasspath();

  public abstract RegularFileProperty getAppApk();

  public abstract RegularFileProperty getTestApk();

  public abstract RegularFileProperty getLibraryTestApk();

  public abstract DirectoryProperty getOutputsDir();

  public abstract ListProperty<Map<String, String>> getDevices();

  public abstract MapProperty<String, String> getEnvironmentVariables();
}
