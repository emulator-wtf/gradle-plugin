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
import java.util.function.Function;

import static java.util.function.Function.identity;

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
    getVersion().convention("0.9.15");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());

    // discover proxy settings from System properties
    sysPropConvention(getProxyHost(), "http.proxyHost");
    sysPropConvention(getProxyPort(), "http.proxyPort", Integer::parseInt);
    sysPropConvention(getProxyUser(), "http.proxyUser");
    sysPropConvention(getProxyPassword(), "http.proxyPassword");

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

  private static void sysPropConvention(Property<String> extProp, String key) {
    sysPropConvention(extProp, key, identity());
  }

  private static <T> void sysPropConvention(Property<T> extProp, String key, Function<String, T> transform) {
    String sysPropValue = System.getProperty(key);
    if (sysPropValue != null && !sysPropValue.isBlank()) {
      try {
        T transformed = transform.apply(sysPropValue);
        if (transformed != null) {
          extProp.convention(transformed);
        }
      } catch (Exception e) {
        // ignore transform failures, e.g. failing to parse an int
      }
    }
  }
}
