package wtf.emulator.exec;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;
import wtf.emulator.DnsOverride;

import java.io.File;

public interface EwConnectivityCheckWorkParameters extends WorkParameters {
  SetProperty<File> getClasspath();

  Property<String> getToken();

  Property<String> getProxyHost();

  Property<Integer> getProxyPort();

  Property<String> getProxyUser();

  Property<String> getProxyPassword();

  ListProperty<String> getDnsServers();

  ListProperty<DnsOverride> getDnsOverrides();

  Property<Boolean> getEgressTunnel();

  Property<String> getEgressLocalhostForwardIp();

  ListProperty<String> getRelays();

  Property<Boolean> getDebug();

  Property<Boolean> getVerbose();

  Property<Boolean> getPrintOutput();
}
