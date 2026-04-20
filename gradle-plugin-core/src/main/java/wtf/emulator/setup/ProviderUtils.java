package wtf.emulator.setup;

import org.gradle.api.Project;
import org.gradle.api.Transformer;
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
  public static void sysPropConvention(Project project, Property<String> extProp, String... keys) {
    sysPropConvention(project, extProp, Arrays.asList(keys), (s) -> s);
  }

  /**
   * Initializes the given property from one of the System properties identified by {@code keys}, in
   * decreasing priority order. The first present System property provider is used; if that property is
   * present with a blank value, fallback to later keys does not occur.
   *
   * @param project the project used to access Gradle providers
   * @param extProp the property to initialize
   * @param keys the System property keys to check, in priority order
   * @param transform transforms the selected System property value before assigning it
   * @param <T> the target property type
   */
  public static <T> void sysPropConvention(Project project, Property<T> extProp, List<String> keys, Transformer<T, String> transform) {
    final var providers = keys.stream().map(key -> project.getProviders().systemProperty(key));
    final var conventionProvider = providers.reduce(Provider::orElse);
    conventionProvider.ifPresent(stringProvider -> extProp.convention(stringProvider.map(transform)));
  }
}
