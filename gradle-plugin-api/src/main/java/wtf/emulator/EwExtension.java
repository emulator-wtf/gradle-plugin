package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.function.Function.identity;

public abstract class EwExtension implements EwInvokeConfiguration {
  private final Property<Integer> variantCount;

  public abstract Property<Boolean> getRepositoryCheckEnabled();

  public abstract Property<String> getVersion();

  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();

  public abstract Property<String> getTestRunnerClass();

  public abstract MapProperty<String, Object> getEnvironmentVariables();

  public abstract MapProperty<String, Object> getSecretEnvironmentVariables();

  public abstract ListProperty<TestReporter> getTestReporters();

  private Action<EwVariantFilter> filter = null;

  private final DomainObjectSet<EwDeviceSpec> devices;

  private final ObjectFactory objectFactory;

  @Inject
  public EwExtension(ObjectFactory objectFactory) {
    getVersion().convention("0.12.1");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());

    // discover proxy settings from System properties
    sysPropConvention(getProxyHost(), "https.proxyHost", "http.proxyHost");
    sysPropConvention(getProxyPort(), Arrays.asList("https.proxyPort", "http.proxyPort"), Integer::parseInt);
    sysPropConvention(getProxyUser(), "https.proxyUser", "http.proxyUser");
    sysPropConvention(getProxyPassword(), "https.proxyPassword", "http.proxyPassword");

    this.objectFactory = objectFactory;
    this.variantCount = objectFactory.property(Integer.class).convention(0);
    this.devices = objectFactory.domainObjectSet(EwDeviceSpec.class);
  }

  @SuppressWarnings("unused")
  public void variantFilter(Action<EwVariantFilter> filter) {
    this.filter = filter;
  }

  public DomainObjectCollection<EwDeviceSpec> getDevices() {
    return this.devices;
  }

  @SuppressWarnings("unused")
  public void device(Action<EwDeviceSpec> action) {
    EwDeviceSpec builder = objectFactory.newInstance(EwDeviceSpec.class);
    action.execute(builder);
    this.devices.add(builder);
  }

  protected Action<EwVariantFilter> getFilter() {
    return this.filter;
  }

  protected Property<Integer> getVariantCount() {
    return this.variantCount;
  }

  private static void sysPropConvention(Property<String> extProp, String... keys) {
    sysPropConvention(extProp, Arrays.asList(keys), identity());
  }

  private static <T> void sysPropConvention(Property<T> extProp, List<String> keys, Function<String, T> transform) {
    for (String key : keys) {
      String sysPropValue = System.getProperty(key);
      if (sysPropValue != null && !sysPropValue.isBlank()) {
        try {
          T transformed = transform.apply(sysPropValue);
          if (transformed != null) {
            extProp.convention(transformed);
            return;
          }
        }
        catch (Exception e) {
          // ignore transform failures, e.g. failing to parse an int
        }
      }
    }
  }
}
