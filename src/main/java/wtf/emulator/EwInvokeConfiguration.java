package wtf.emulator;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.time.Duration;

public interface EwInvokeConfiguration {
  public abstract ListProperty<OutputType> getOutputs();

  public abstract Property<Boolean> getUseOrchestrator();
  public abstract Property<Boolean> getClearPackageData();
  public abstract Property<Boolean> getWithCoverage();

  public abstract Property<FileCollection> getAdditionalApks();

  public abstract Property<Integer> getNumUniformShards();
  public abstract Property<Integer> getNumShards();
  public abstract Property<Integer> getNumBalancedShards();

  public abstract ListProperty<String> getDirectoriesToPull();

  public abstract Property<Boolean> getSideEffects();

  public abstract Property<Duration> getTimeout();

  public abstract Property<Boolean> getFileCacheEnabled();

  public abstract Property<Duration> getFileCacheTtl();

  public abstract Property<Boolean> getTestCacheEnabled();

  public abstract Property<Integer> getNumFlakyTestAttempts();
}
