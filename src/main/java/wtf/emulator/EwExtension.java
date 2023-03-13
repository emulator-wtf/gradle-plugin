package wtf.emulator;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class EwExtension {
  public abstract Property<Boolean> getRepositoryCheckEnabled();
  public abstract Property<String> getVersion();
  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();
  public abstract ListProperty<OutputType> getOutputs();

  public abstract ListProperty<Map<String, Object>> getDevices();

  public abstract Property<Boolean> getUseOrchestrator();
  public abstract Property<Boolean> getClearPackageData();
  public abstract Property<Boolean> getWithCoverage();

  public abstract Property<FileCollection> getAdditionalApks();
  public abstract MapProperty<String, Object> getEnvironmentVariables();

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

  public EwExtension() {
    getVersion().convention("0.9.1");
    getSideEffects().convention(false);
    getOutputs().convention(Collections.emptyList());
  }
}
