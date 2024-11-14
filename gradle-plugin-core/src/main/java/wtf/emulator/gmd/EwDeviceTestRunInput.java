package wtf.emulator.gmd;

import com.android.build.api.instrumentation.manageddevice.DeviceTestRunInput;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import wtf.emulator.FlakyRepeatMode;
import wtf.emulator.GpuMode;
import wtf.emulator.OutputType;

import java.io.File;
import java.time.Duration;
import java.util.Map;

public abstract class EwDeviceTestRunInput implements DeviceTestRunInput {

  @Optional
  @Input
  public abstract Property<String> getDevice();

  @Optional
  @Input
  public abstract Property<Integer> getApiLevel();

  @Optional
  @Input
  public abstract Property<GpuMode> getGpu();

  // Extension properties from emulatorwtf{} block that are relevant for GMD test runs

  @Input
  public abstract Property<String> getToken();

  @Optional
  @OutputDirectory
  public abstract DirectoryProperty getOutputsDir();

  @Optional
  @OutputFile
  public Provider<RegularFile> getMergedXml() {
    return getOutputsDir().file("results.xml");
  }

  @Optional
  @Input
  public abstract ListProperty<OutputType> getOutputTypes();

  @Optional
  @Input
  public abstract Property<Boolean> getRecordVideo();

  @Optional
  @Input
  public abstract ListProperty<Map<String, String>> getDevices();

  @Optional
  @Input
  public abstract Property<Boolean> getUseOrchestrator();

  @Optional
  @Input
  public abstract Property<Boolean> getClearPackageData();

  @Optional
  @Input
  public abstract Property<Boolean> getWithCoverage();

  @Optional
  @InputFiles
  @PathSensitive(PathSensitivity.NONE)
  public abstract Property<FileCollection> getAdditionalApks();

  @Optional
  @Input
  public abstract MapProperty<String, String> getEnvironmentVariables();

  @Optional
  @Input
  public abstract MapProperty<String, String> getSecretEnvironmentVariables();

  @Optional
  @Input
  public abstract Property<Integer> getNumUniformShards();

  @Optional
  @Input
  public abstract Property<Integer> getNumBalancedShards();

  @Optional
  @Input
  public abstract Property<Integer> getNumShards();

  @Optional
  @Input
  public abstract Property<Integer> getShardTargetRuntime();

  @Optional
  @Input
  public abstract ListProperty<String> getDirectoriesToPull();

  @Optional
  @Input
  public abstract Property<Boolean> getSideEffects();

  @Optional
  @Input
  public abstract Property<Duration> getTestTimeout();

  @Optional
  @Input
  public abstract Property<Boolean> getFileCacheEnabled();

  @Optional
  @Input
  public abstract Property<Duration> getFileCacheTtl();

  @Optional
  @Input
  public abstract Property<Boolean> getTestCacheEnabled();

  @Optional
  @Input
  public abstract Property<Integer> getNumFlakyTestAttempts();

  @Optional
  @Input
  public abstract Property<FlakyRepeatMode> getFlakyTestRepeatMode();

  @Optional
  @Input
  public abstract Property<String> getDisplayName();

  @Optional
  @Input
  public abstract ListProperty<String> getDnsServers();

  @Optional
  @Input
  public abstract Property<Boolean> getEgressTunnel();

  @Optional
  @Input
  public abstract Property<String> getEgressLocalhostForwardIp();

  @Optional
  @Input
  public abstract Property<String> getScmUrl();

  @Optional
  @Input
  public abstract Property<String> getScmCommitHash();

  @Optional
  @Input
  public abstract Property<String> getScmRefName();

  @Optional
  @Input
  public abstract Property<String> getScmPrUrl();

  @Optional
  @Input
  public abstract Property<Boolean> getIgnoreFailures();

  @Optional
  @Input
  public abstract Property<Boolean> getAsync();

  @Internal
  public abstract RegularFileProperty getWorkingDir();

  @Optional
  @Input
  public abstract Property<Boolean> getPrintOutput();

  @Optional
  @Input
  public abstract Property<String> getTestTargets();

  @Optional
  @Input
  public abstract Property<String> getProxyHost();

  @Optional
  @Input
  public abstract Property<Integer> getProxyPort();

  @Optional
  @Input
  public abstract Property<String> getProxyUser();

  @Optional
  @Input
  public abstract Property<String> getProxyPassword();

  @Input
  public abstract SetProperty<File> getClasspath();

  @Internal
  public abstract DirectoryProperty getIntermediatesOutputs();

}
