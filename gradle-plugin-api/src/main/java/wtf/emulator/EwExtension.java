package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.function.Function.identity;

public abstract class EwExtension implements EwInvokeConfiguration {
  private final Property<Integer> variantCount;

  private final Property<Boolean> useOrchestratorAndroidDsl;

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
    getVersion().convention(BuildConfig.EW_CLI_VERSION);
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());

    // discover proxy settings from System properties
    sysPropConvention(getProxyHost(), "https.proxyHost", "http.proxyHost");
    sysPropConvention(getProxyPort(), Arrays.asList("https.proxyPort", "http.proxyPort"), Integer::parseInt);
    sysPropConvention(getProxyUser(), "https.proxyUser", "http.proxyUser");
    sysPropConvention(getProxyPassword(), "https.proxyPassword", "http.proxyPassword");
    nonProxyHostsConvention(getNonProxyHosts());

    this.objectFactory = objectFactory;
    this.variantCount = objectFactory.property(Integer.class).convention(0);
    this.useOrchestratorAndroidDsl = objectFactory.property(Boolean.class).convention(false);
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

  @SuppressWarnings("unused")
  public void targets(Action<TestTargetsSpec> action) {
    final var obj = objectFactory.newInstance(TestTargetsSpec.class);
    action.execute(obj);
    getTestTargets().set(obj);
  }

  protected Action<EwVariantFilter> getFilter() {
    return this.filter;
  }

  protected Property<Integer> getVariantCount() {
    return this.variantCount;
  }

  protected Property<Boolean> getUseOrchestratorAndroidDsl() {
    return this.useOrchestratorAndroidDsl;
  }

  private static void nonProxyHostsConvention(ListProperty<String> property) {
    String sysPropValue = System.getProperty("http.nonProxyHosts");
    if (sysPropValue != null && !sysPropValue.isBlank()) {
      // drop the localhost and loopback entries as they are not relevant for no_proxy
      Set<String> ignoredHosts = new HashSet<>(Arrays.asList("localhost", "127.*", "[::1]"));

      // Java's http.nonProxyHosts uses '|' as a separator and allows '*' wildcards
      String[] parts = sysPropValue.split("\\|");
      List<String> values = mapNonProxyHosts(parts, ignoredHosts);

      if (!values.isEmpty()) {
        property.convention(values);
      }
    }
  }

  /**
   * Maps Java-style non-proxy hosts to no_proxy style.
   * NOTE: This does not cover all edge cases, just the common ones. For instance, wildcards
   * in the middle of a hostname are not supported in no_proxy and thus ignored.
   */
  private static @NotNull List<String> mapNonProxyHosts(String[] parts, Set<String> ignoredHosts) {
    List<String> values = new ArrayList<>(parts.length);
    for (String part : parts) {
      String t = part.trim();
      if (t.isEmpty()) continue;

      // ignore the default loopback entries
      if (ignoredHosts.contains(t)) {
        continue;
      }

      // Keep global wildcard as-is
      if ("*".equals(t)) {
        values.add("*");
        continue;
      }

      if (t.startsWith("*")) {
        // Any leading '*' means a suffix match; convert to leading dot
        t = t.substring(1);
        if (!t.isEmpty() && !t.startsWith(".")) {
          t = "." + t;
        }
      } else if (t.contains("*")) {
        // wildcards in the middle are not supported in no_proxy, skip
        continue;
      }

      values.add(t);
    }
    return values;
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
