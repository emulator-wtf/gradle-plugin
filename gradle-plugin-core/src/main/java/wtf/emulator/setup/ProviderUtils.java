package wtf.emulator.setup;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import wtf.emulator.EwDeviceSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class ProviderUtils {
  public static Provider<Map<String, String>> deviceToCliMap(ProviderFactory providers, EwDeviceSpec device) {
    var entryProviders = new ArrayList<Provider<Map.Entry<String, String>>>();
    entryProviders.add(device.getModel().orElse(EwDeviceSpec.DEFAULT_MODEL).map(val -> Map.entry("model", val.getCliValue())));
    entryProviders.add(device.getVersion().orElse(EwDeviceSpec.DEFAULT_VERSION).map(val -> Map.entry("version", Integer.toString(val))));

    if (device.getLocale().isPresent()) {
      entryProviders.add(device.getLocale().map(val -> Map.entry("locale", val)));
    }

    entryProviders.add(device.getGpu().orElse(EwDeviceSpec.DEFAULT_GPU).map(val -> Map.entry("gpu", val.getCliValue())));

    var entriesProvider = ProviderUtils.reduce(
      providers.provider(() -> new ArrayList<Map.Entry<String, String>>()),
      entryProviders,
      (acc, entry) -> new ArrayList<>(Stream.concat(acc.stream(), Stream.of(entry)).toList())
    );

    return entriesProvider.map(entries -> {
      var map = new HashMap<String, String>();
      for (var entry : entries) {
        map.put(entry.getKey(), entry.getValue());
      }
      return map;
    });
  }

  public static <R, T> Provider<R> reduce(Provider<R> initialValue, Iterable<Provider<T>> providers, BiFunction<R, T, R> reducer) {
    var acc = initialValue;
    for (Provider<T> provider : providers) {
      acc = acc.zip(provider, reducer);
    }
    return acc;
  }

  /**
   * Variant of {@code sysPropConvention} that directly operates with Strings without an intermediate transform
   * step.
   */
  public static void sysPropConvention(Property<String> extProp, String... keys) {
    sysPropConvention(extProp, Arrays.asList(keys), identity());
  }

  /**
   * Initializes the given {@param extProp} with one of the System properties defined by {@param keys},
   * with decreasing priority, i.e. the first non-blank System property value is used.
   */
  public static <T> void sysPropConvention(Property<T> extProp, List<String> keys, Function<String, T> transform) {
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
