package wtf.emulator;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.time.Duration;

public interface EwInvokeConfiguration {
  ListProperty<OutputType> getOutputs();
  Property<Boolean> getRecordVideo();

  Property<Boolean> getUseOrchestrator();
  Property<Boolean> getClearPackageData();
  Property<Boolean> getWithCoverage();

  Property<FileCollection> getAdditionalApks();

  Property<Integer> getNumUniformShards();
  Property<Integer> getNumShards();
  Property<Integer> getNumBalancedShards();

  ListProperty<String> getDirectoriesToPull();

  Property<Boolean> getSideEffects();

  Property<Duration> getTimeout();

  Property<Boolean> getFileCacheEnabled();

  Property<Duration> getFileCacheTtl();

  Property<Boolean> getTestCacheEnabled();

  Property<Integer> getNumFlakyTestAttempts();

  Property<String> getDisplayName();

  Property<String> getScmUrl();

  Property<String> getScmCommitHash();

  Property<Boolean> getIgnoreFailures();

  Property<Boolean> getAsync();

  Property<Boolean> getPrintOutput();

  Property<String> getTestTargets();
}
