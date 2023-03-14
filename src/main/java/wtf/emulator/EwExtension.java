package wtf.emulator;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.util.Collections;
import java.util.Map;

public abstract class EwExtension implements EwInvokeConfiguration {
  public abstract Property<Boolean> getRepositoryCheckEnabled();

  public abstract Property<String> getVersion();

  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();

  public abstract ListProperty<Map<String, Object>> getDevices();

  public abstract MapProperty<String, Object> getEnvironmentVariables();

  public EwExtension() {
    getVersion().convention("0.9.1");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());
  }
}
