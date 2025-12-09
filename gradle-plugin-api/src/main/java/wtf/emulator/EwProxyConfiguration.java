package wtf.emulator;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public interface EwProxyConfiguration {
  /**
   * Explicitly set the proxy host to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyHost();

  /**
   * Explicitly set the proxy port to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<Integer> getProxyPort();

  /**
   * Explicitly set the proxy username to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyUser();

  /**
   * Explicitly set the proxy password to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyPassword();

  /**
   * Explicitly set the non-proxy hosts to use for communicating with emulator.wtf backend.
   * The proxy format is the same as no_proxy environment variable in UNIX systems, e.g.
   * "*" or a list of domain suffixes. Leading dot is optional, e.g. ".example.com" and
   * "example.com" are equivalent.
   * By default the Java system properties are used for discovering proxy settings.
   * If not set then non-proxy hosts are based on the no_proxy environment variable if present.
   */
  ListProperty<String> getNonProxyHosts();
}
