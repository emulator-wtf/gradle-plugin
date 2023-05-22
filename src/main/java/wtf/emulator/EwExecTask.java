package wtf.emulator;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

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
  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getAppApk();

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
  public abstract Property<Integer> getNumUniformShards();

  @Optional
  @Input
  public abstract Property<Integer> getNumBalancedShards();

  @Optional
  @Input
  public abstract Property<Integer> getNumShards();

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
  public abstract Property<String> getDisplayName();

  @Optional
  @Input
  public abstract Property<String> getScmUrl();

  @Optional
  @Input
  public abstract Property<String> getScmCommitHash();

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

  @Internal
  public abstract RegularFileProperty getOutputFailureFile();

  @Optional
  @Input
  public abstract Property<Boolean> getPrintOutput();

  @TaskAction
  public void runTests() {
    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    workQueue.submit(EwWorkAction.class, p -> {
      p.getClasspath().set(getClasspath().get().getFiles());
      p.getToken().set(getToken());
      p.getAppApk().set(getAppApk());
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
      p.getEnvironmentVariables().set(getEnvironmentVariables());
      p.getNumUniformShards().set(getNumUniformShards());
      p.getNumBalancedShards().set(getNumBalancedShards());
      p.getNumShards().set(getNumShards());
      p.getDirectoriesToPull().set(getDirectoriesToPull());
      p.getSideEffects().set(getSideEffects());
      p.getTimeout().set(getTestTimeout());
      p.getFileCacheEnabled().set(getFileCacheEnabled());
      p.getFileCacheTtl().set(getFileCacheTtl());
      p.getTestCacheEnabled().set(getTestCacheEnabled());
      p.getNumFlakyTestAttempts().set(getNumFlakyTestAttempts());
      p.getDisplayName().set(getDisplayName());
      p.getScmUrl().set(getScmUrl());
      p.getScmCommitHash().set(getScmCommitHash());
      p.getWorkingDir().set(getWorkingDir());
      p.getIgnoreFailures().set(getIgnoreFailures());
      p.getOutputFailureFile().set(getOutputFailureFile());
      p.getAsync().set(getAsync());
      p.getPrintOutput().set(getPrintOutput());
    });
  }
}
