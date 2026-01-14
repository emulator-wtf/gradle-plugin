package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;

public abstract class EwConfiguration extends EwInvokeDsl implements Named {
  private Action<EwVariantFilter> defaultFilter = null;
  private DomainObjectSet<EwDeviceSpec> defaultDevices = null;

  void extendFrom(EwInvokeDsl defaults) {
    this.defaultFilter = defaults.getFilterOrDefault();
    this.defaultDevices = defaults.getDevices();

    // init base props
    getTestRunnerClass().convention(defaults.getTestRunnerClass());
    getEnvironmentVariables().convention(defaults.getEnvironmentVariables());
    getSecretEnvironmentVariables().convention(defaults.getSecretEnvironmentVariables());
    getTestReporters().convention(defaults.getTestReporters());

    // init invoke props
    getOutputs().convention(defaults.getOutputs());
    getRecordVideo().convention(defaults.getRecordVideo());
    getUseOrchestrator().convention(defaults.getUseOrchestrator());
    getClearPackageData().convention(defaults.getClearPackageData());
    getWithCoverage().convention(defaults.getWithCoverage());
    getAdditionalApks().convention(defaults.getAdditionalApks());
    getNumUniformShards().convention(defaults.getNumUniformShards());
    getNumShards().convention(defaults.getNumShards());
    getNumBalancedShards().convention(defaults.getNumBalancedShards());
    getShardTargetRuntime().convention(defaults.getShardTargetRuntime());
    getDirectoriesToPull().convention(defaults.getDirectoriesToPull());
    getSideEffects().convention(defaults.getSideEffects());
    getTimeout().convention(defaults.getTimeout());
    getFileCacheEnabled().convention(defaults.getFileCacheEnabled());
    getFileCacheTtl().convention(defaults.getFileCacheTtl());
    getTestCacheEnabled().convention(defaults.getTestCacheEnabled());
    getNumFlakyTestAttempts().convention(defaults.getNumFlakyTestAttempts());
    getFlakyTestRepeatMode().convention(defaults.getFlakyTestRepeatMode());
    getDisplayName().convention(defaults.getDisplayName());
    getDnsServers().convention(defaults.getDnsServers());
    getEgressTunnel().convention(defaults.getEgressTunnel());
    getDnsOverrides().convention(defaults.getDnsOverrides());
    getRelays().convention(defaults.getRelays());
    getEgressLocalhostForwardIp().convention(defaults.getEgressLocalhostForwardIp());
    getScmUrl().convention(defaults.getScmUrl());
    getScmCommitHash().convention(defaults.getScmCommitHash());
    getScmRefName().convention(defaults.getScmRefName());
    getScmPrUrl().convention(defaults.getScmPrUrl());
    getIgnoreFailures().convention(defaults.getIgnoreFailures());
    getAsync().convention(defaults.getAsync());
    getPrintOutput().convention(defaults.getPrintOutput());
    getTestTargets().convention(defaults.getTestTargets());

    // init proxy props
    getProxyHost().convention(defaults.getProxyHost());
    getProxyPort().convention(defaults.getProxyPort());
    getProxyUser().convention(defaults.getProxyUser());
    getProxyPassword().convention(defaults.getProxyPassword());
    getNonProxyHosts().convention(defaults.getNonProxyHosts());
  }

  @Override
  protected DomainObjectSet<EwDeviceSpec> getDevicesOrDefault() {
    if (getDevices().isEmpty() && defaultDevices != null) {
      return defaultDevices;
    }
    return getDevices();
  }

  @Override
  protected Action<EwVariantFilter> getFilterOrDefault() {
    if (getFilter() == null && defaultFilter != null) {
      return defaultFilter;
    }
    return getFilter();
  }
}
