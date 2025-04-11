package wtf.emulator;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.time.Duration;

public interface EwInvokeConfiguration {
  // files
  Property<FileCollection> getAdditionalApks();
  Property<Duration> getFileCacheTtl();
  Property<Boolean> getFileCacheEnabled();

  // test config
  Property<String> getDisplayName();
  Property<Boolean> getUseOrchestrator();
  Property<Boolean> getClearPackageData();
  Property<Integer> getNumFlakyTestAttempts();
  Property<String> getFlakyTestRepeatMode();
  Property<Duration> getTimeout();
  Property<String> getTestTargets();
  Property<Boolean> getTestCacheEnabled();
  Property<Boolean> getSideEffects();

  // sharding
  Property<Integer> getNumBalancedShards();
  Property<Integer> getShardTargetRuntime();
  Property<Integer> getNumUniformShards();
  Property<Integer> getNumShards();

  // test outputs
  Property<Boolean> getRecordVideo();
  Property<Boolean> getWithCoverage();
  ListProperty<String> getDirectoriesToPull();
  ListProperty<OutputType> getOutputs();

  // emulator networking
  ListProperty<String> getDnsServers();
  Property<Boolean> getEgressTunnel();
  Property<String> getEgressLocalhostForwardIp();

  // source control
  Property<String> getScmUrl();
  Property<String> getScmCommitHash();
  Property<String> getScmPrUrl();
  Property<String> getScmRefName();

  // ew networking
  Property<String> getProxyHost();
  Property<Integer> getProxyPort();
  Property<String> getProxyUser();
  Property<String> getProxyPassword();

  // execution
  Property<Boolean> getAsync();
  Property<Boolean> getIgnoreFailures();
  Property<Boolean> getPrintOutput();
}
