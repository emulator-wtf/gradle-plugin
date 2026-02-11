package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import wtf.emulator.setup.TargetUtils;

public class DslInternals {
  public static Action<EwVariantFilter> getFilter(EwInvokeDsl dsl) {
    return dsl.getFilterOrDefault();
  }

  public static DomainObjectSet<EwDeviceSpec> getDevices(EwInvokeDsl dsl) {
    return dsl.getDevicesOrDefault();
  }

  public static void extendConfiguration(EwConfiguration config, EwInvokeDsl defaults) {
    // custom handling for filter and devices
    config.setDefaultFilter(defaults.getFilterOrDefault());
    config.setDefaultDevices(defaults.getDevicesOrDefault());

    // init base props
    config.getTestRunnerClass().convention(defaults.getTestRunnerClass());
    config.getEnvironmentVariables().convention(defaults.getEnvironmentVariables());
    config.getSecretEnvironmentVariables().convention(defaults.getSecretEnvironmentVariables());
    config.getTestReporters().convention(defaults.getTestReporters());

    // init invoke props
    config.getOutputs().convention(defaults.getOutputs());
    config.getRecordVideo().convention(defaults.getRecordVideo());
    config.getUseOrchestrator().convention(defaults.getUseOrchestrator());
    config.getClearPackageData().convention(defaults.getClearPackageData());
    config.getWithCoverage().convention(defaults.getWithCoverage());
    config.getAdditionalApks().convention(defaults.getAdditionalApks());
    config.getNumUniformShards().convention(defaults.getNumUniformShards());
    config.getNumShards().convention(defaults.getNumShards());
    config.getNumBalancedShards().convention(defaults.getNumBalancedShards());
    config.getShardTargetRuntime().convention(defaults.getShardTargetRuntime());
    config.getTestcaseDurationHint().convention(defaults.getTestcaseDurationHint());
    config.getDirectoriesToPull().convention(defaults.getDirectoriesToPull());
    config.getSideEffects().convention(defaults.getSideEffects());
    config.getTimeout().convention(defaults.getTimeout());
    config.getFileCacheEnabled().convention(defaults.getFileCacheEnabled());
    config.getFileCacheTtl().convention(defaults.getFileCacheTtl());
    config.getTestCacheEnabled().convention(defaults.getTestCacheEnabled());
    config.getNumFlakyTestAttempts().convention(defaults.getNumFlakyTestAttempts());
    config.getFlakyTestRepeatMode().convention(defaults.getFlakyTestRepeatMode());
    config.getDisplayName().convention(defaults.getDisplayName());
    config.getDnsServers().convention(defaults.getDnsServers());
    config.getEgressTunnel().convention(defaults.getEgressTunnel());
    config.getDnsOverrides().convention(defaults.getDnsOverrides());
    config.getRelays().convention(defaults.getRelays());
    config.getEgressLocalhostForwardIp().convention(defaults.getEgressLocalhostForwardIp());
    config.getScmUrl().convention(defaults.getScmUrl());
    config.getScmCommitHash().convention(defaults.getScmCommitHash());
    config.getScmRefName().convention(defaults.getScmRefName());
    config.getScmPrUrl().convention(defaults.getScmPrUrl());
    config.getIgnoreFailures().convention(defaults.getIgnoreFailures());
    config.getAsync().convention(defaults.getAsync());
    config.getPrintOutput().convention(defaults.getPrintOutput());
    config.getTestTargetsString().convention(
      // first targets from this config
      config.getTestTargets().map(TargetUtils::toCliString)
        // then fall back to defaults string
        .orElse(defaults.getTestTargetsString()));

    // init proxy props
    config.getProxyHost().convention(defaults.getProxyHost());
    config.getProxyPort().convention(defaults.getProxyPort());
    config.getProxyUser().convention(defaults.getProxyUser());
    config.getProxyPassword().convention(defaults.getProxyPassword());
    config.getNonProxyHosts().convention(defaults.getNonProxyHosts());
  }
}
