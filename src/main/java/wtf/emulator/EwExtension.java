package wtf.emulator;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.util.Map;

public abstract class EwExtension {
  public abstract Property<String> getVersion();
  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();

  public abstract ListProperty<Map<String, Object>> getDevices();

  public abstract Property<Boolean> getUseOrchestrator();
  public abstract Property<Boolean> getClearPackageData();
  public abstract Property<Boolean> getWithCoverage();

  public abstract Property<FileCollection> getAdditionalApks();
  public abstract MapProperty<String, String> getEnvironmentVariables();

  public abstract Property<Integer> getNumUniformShards();
  public abstract Property<Integer> getNumShards();

  public abstract ListProperty<String> getDirectoriesToPull();

  public EwExtension() {
    getVersion().convention("0.0.24");
  }
}
