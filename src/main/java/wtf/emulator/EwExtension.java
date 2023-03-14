package wtf.emulator;

import org.gradle.api.Action;
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

  private Action<EwVariantFilter> filter = null;

  public EwExtension() {
    getVersion().convention("0.9.2");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());
  }

  public void variantFilter(Action<EwVariantFilter> filter) {
    this.filter = filter;
  }

  protected Action<EwVariantFilter> getFilter() {
    return this.filter;
  }
}
