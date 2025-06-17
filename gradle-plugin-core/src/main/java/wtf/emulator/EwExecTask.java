package wtf.emulator;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import wtf.emulator.exec.EwWorkAction;
import wtf.emulator.exec.EwWorkParameters;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Map;

@CacheableTask
public abstract class EwExecTask extends DefaultTask {
  @Classpath
  @InputFiles
  public abstract Property<FileCollection> getClasspath();

  @Input
  public abstract Property<String> getToken();

  @Optional
  @InputFiles
  @PathSensitive(PathSensitivity.NONE)
  public abstract Property<FileCollection> getApks();

  @Optional
  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getTestApk();

  @Optional
  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getLibraryTestApk();

  @Optional
  @OutputDirectory
  public abstract DirectoryProperty getOutputsDir();

  @Optional
  @OutputFile
  public Provider<RegularFile> getMergedXml() {
    return getOutputsDir().file("results.xml");
  }

  @OutputFile
  public abstract RegularFileProperty getOutputFile();

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
  public abstract Property<String> getInstrumentationRunner();

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

  @Inject
  public abstract WorkerExecutor getWorkerExecutor();

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

  @TaskAction
  public void runTests() {
    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    workQueue.submit(EwWorkAction.class, this::fillWorkParameters);
  }

  protected void fillWorkParameters(EwWorkParameters p) {
    p.getClasspath().set(getClasspath().get().getFiles());
    p.getToken().set(getToken());
    p.getApks().set(getApks());
    p.getTestApk().set(getTestApk());
    p.getLibraryTestApk().set(getLibraryTestApk());
    p.getOutputsDir().set(getOutputsDir());
    p.getOutputs().set(getOutputTypes());
    p.getRecordVideo().set(getRecordVideo());
    p.getDevices().set(getDevices());
    p.getUseOrchestrator().set(getUseOrchestrator());
    p.getClearPackageData().set(getClearPackageData());
    p.getWithCoverage().set(getWithCoverage());
    p.getAdditionalApks().set(getAdditionalApks());
    p.getInstrumentationRunner().set(getInstrumentationRunner());
    p.getEnvironmentVariables().set(getEnvironmentVariables());
    p.getSecretEnvironmentVariables().set(getSecretEnvironmentVariables());
    p.getNumUniformShards().set(getNumUniformShards());
    p.getNumBalancedShards().set(getNumBalancedShards());
    p.getNumShards().set(getNumShards());
    p.getShardTargetRuntime().set(getShardTargetRuntime());
    p.getDirectoriesToPull().set(getDirectoriesToPull());
    p.getSideEffects().set(getSideEffects());
    p.getTimeout().set(getTestTimeout());
    p.getFileCacheEnabled().set(getFileCacheEnabled());
    p.getFileCacheTtl().set(getFileCacheTtl());
    p.getTestCacheEnabled().set(getTestCacheEnabled());
    p.getNumFlakyTestAttempts().set(getNumFlakyTestAttempts());
    p.getFlakyTestRepeatMode().set(getFlakyTestRepeatMode());
    p.getDisplayName().set(getDisplayName());
    p.getDnsServers().set(getDnsServers());
    p.getEgressTunnel().set(getEgressTunnel());
    p.getEgressLocalhostForwardIp().set(getEgressLocalhostForwardIp());
    p.getScmUrl().set(getScmUrl());
    p.getScmCommitHash().set(getScmCommitHash());
    p.getScmRefName().set(getScmRefName());
    p.getScmPrUrl().set(getScmPrUrl());
    p.getWorkingDir().set(getWorkingDir());
    p.getIgnoreFailures().set(getIgnoreFailures());
    p.getAsync().set(getAsync());
    p.getPrintOutput().set(getPrintOutput());
    p.getTestTargets().set(getTestTargets());
    p.getProxyHost().set(getProxyHost());
    p.getProxyPort().set(getProxyPort());
    p.getProxyUser().set(getProxyUser());
    p.getProxyPassword().set(getProxyPassword());
    p.getOutputFile().set(getOutputFile());
    p.getTaskPath().set(getPath());
  }
}
