package wtf.emulator;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import wtf.emulator.exec.EwConnectivityCheckWorkAction;
import wtf.emulator.exec.EwConnectivityCheckWorkParameters;

import javax.inject.Inject;

public abstract class EwConnectivityCheckTask extends DefaultTask {
  @Classpath
  @InputFiles
  public abstract Property<FileCollection> getClasspath();

  @Input
  public abstract Property<String> getToken();

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

  @Optional
  @Input
  public abstract ListProperty<String> getNonProxyHosts();

  @Optional
  @Input
  public abstract ListProperty<String> getDnsServers();

  @Optional
  @Input
  public abstract ListProperty<DnsOverride> getDnsOverrides();

  @Optional
  @Input
  public abstract ListProperty<String> getRelays();

  @Optional
  @Input
  public abstract Property<Boolean> getEgressTunnel();

  @Optional
  @Input
  public abstract Property<String> getEgressLocalhostForwardIp();

  @Optional
  @Input
  public abstract Property<Boolean> getDebug();

  @Optional
  @Input
  public abstract Property<Boolean> getVerbose();

  @Optional
  @Input
  public abstract Property<Boolean> getPrintOutput();

  @Inject
  public abstract WorkerExecutor getWorkerExecutor();

  @TaskAction
  void run() {
    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    workQueue.submit(EwConnectivityCheckWorkAction.class, this::fillWorkParameters);
  }

  protected void fillWorkParameters(EwConnectivityCheckWorkParameters p) {
    p.getClasspath().set(getClasspath().get().getFiles());
    p.getToken().set(getToken());
    p.getProxyHost().set(getProxyHost());
    p.getProxyPort().set(getProxyPort());
    p.getProxyUser().set(getProxyUser());
    p.getProxyPassword().set(getProxyPassword());
    p.getNonProxyHosts().set(getNonProxyHosts());
    p.getDnsServers().set(getDnsServers());
    p.getDnsOverrides().set(getDnsOverrides());
    p.getEgressTunnel().set(getEgressTunnel());
    p.getEgressLocalhostForwardIp().set(getEgressLocalhostForwardIp());
    p.getRelays().set(getRelays());
    p.getDebug().set(getDebug());
    p.getVerbose().set(getVerbose());
    p.getPrintOutput().set(getPrintOutput());
  }
}
