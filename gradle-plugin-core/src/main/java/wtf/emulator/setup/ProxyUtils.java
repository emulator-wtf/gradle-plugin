package wtf.emulator.setup;

import org.gradle.api.provider.ListProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProxyUtils {
  /**
   * Initializes the given {@param property} with a list of domains in the `no_proxy` format as a convention,
   * based on the `http.nonProxyHosts` system property.
   */
  public static void nonProxyHostsConvention(ListProperty<String> property) {
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
}
