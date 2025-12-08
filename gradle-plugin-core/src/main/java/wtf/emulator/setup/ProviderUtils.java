package wtf.emulator.setup;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import wtf.emulator.EwDeviceSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ProviderUtils {
  public static Provider<Map<String, String>> deviceToCliMap(ProviderFactory providers, EwDeviceSpec device) {
    var entryProviders = List.of(
      device.getModel().orElse(EwDeviceSpec.DEFAULT_MODEL).map(val -> Map.entry("model", val.getCliValue())),
      device.getVersion().orElse(EwDeviceSpec.DEFAULT_VERSION).map(val -> Map.entry("version", Integer.toString(val))),
      device.getGpu().orElse(EwDeviceSpec.DEFAULT_GPU).map(val -> Map.entry("gpu", val.getCliValue()))
    );

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
}
