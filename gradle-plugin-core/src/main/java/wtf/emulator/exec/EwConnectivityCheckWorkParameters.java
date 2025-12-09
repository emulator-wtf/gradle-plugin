package wtf.emulator.exec;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;
import wtf.emulator.DnsOverride;
import wtf.emulator.EwProxyConfiguration;

import java.io.File;

public interface EwConnectivityCheckWorkParameters extends WorkParameters, EwProxyConfiguration {
  SetProperty<File> getClasspath();

  Property<String> getToken();

  ListProperty<String> getDnsServers();

  ListProperty<DnsOverride> getDnsOverrides();

  Property<Boolean> getEgressTunnel();

  Property<String> getEgressLocalhostForwardIp();

  ListProperty<String> getRelays();

  Property<Boolean> getDebug();

  Property<Boolean> getVerbose();

  Property<Boolean> getPrintOutput();
}
