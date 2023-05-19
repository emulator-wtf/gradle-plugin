package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

public abstract class EwExtension implements EwInvokeConfiguration {
  private final Property<Integer> variantCount;

  public abstract Property<Boolean> getRepositoryCheckEnabled();

  public abstract Property<String> getVersion();

  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();

  public abstract ListProperty<Map<String, Object>> getDevices();

  public abstract MapProperty<String, Object> getEnvironmentVariables();

  private Action<EwVariantFilter> filter = null;

  @Inject
  public EwExtension(ObjectFactory objectFactory) {
    getVersion().convention("0.9.11");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());
    this.variantCount = objectFactory.property(Integer.class).convention(0);
  }

  @SuppressWarnings("unused")
  public void variantFilter(Action<EwVariantFilter> filter) {
    this.filter = filter;
  }

  protected Action<EwVariantFilter> getFilter() {
    return this.filter;
  }

  protected Property<Integer> getVariantCount() {
    return this.variantCount;
  }
}
