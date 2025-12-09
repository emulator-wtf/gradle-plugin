package wtf.emulator.gmd;

import com.android.build.api.instrumentation.manageddevice.DeviceTestRunConfigureAction;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.TestExtension;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.jetbrains.annotations.NotNull;
import wtf.emulator.EwExtension;
import wtf.emulator.setup.ProjectConfigurator;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class EwDeviceTestRunConfigureAction implements DeviceTestRunConfigureAction<EwManagedDevice, EwDeviceTestRunInput> {

  @Inject
  public abstract ObjectFactory getObjectFactory();

  @Inject
  public abstract Project getProject();

  @NotNull
  @Override
  public EwDeviceTestRunInput configureTaskInput(@NotNull EwManagedDevice ewManagedDevice) {
    EwDeviceTestRunInput deviceTestRunInput = getObjectFactory().newInstance(EwDeviceTestRunInput.class);

    // Configure device-specific properties
    deviceTestRunInput.getDevice().set(ewManagedDevice.getDevice());
    deviceTestRunInput.getDevice().disallowChanges();

    deviceTestRunInput.getApiLevel().set(ewManagedDevice.getApiLevel());
    deviceTestRunInput.getApiLevel().disallowChanges();

    deviceTestRunInput.getGpu().set(ewManagedDevice.getGpu());
    deviceTestRunInput.getGpu().disallowChanges();

    // Get the emulatorwtf extension and grab the relevant properties from there
    EwExtension ext = getProject().getExtensions().getByType(EwExtension.class);

    deviceTestRunInput.getToken().set(ext.getToken().orElse(getProject().provider(() ->
      System.getenv("EW_API_TOKEN"))));
    deviceTestRunInput.getToken().disallowChanges();

    deviceTestRunInput.getWorkingDir().set(getProject().getRootDir());
    deviceTestRunInput.getWorkingDir().disallowChanges();

    deviceTestRunInput.getSecretEnvironmentVariables().convention(Map.of()).set(
      ext.getSecretEnvironmentVariables().map(secretVars -> {
        final Map<String, String> out = new HashMap<>();
        secretVars.forEach((key, value) -> out.put(key, Objects.toString(value)));
        return out;
      })
    );
    deviceTestRunInput.getSecretEnvironmentVariables().disallowChanges();

    deviceTestRunInput.getOutputTypes().set(ext.getOutputs());
    deviceTestRunInput.getOutputTypes().disallowChanges();

    deviceTestRunInput.getRecordVideo().set(ext.getRecordVideo());
    deviceTestRunInput.getRecordVideo().disallowChanges();

    BaseExtension androidExtension = getAndroidExtension(getProject());
    deviceTestRunInput.getUseOrchestrator().set(ext.getUseOrchestrator().orElse(getProject().provider(() -> {
      if (androidExtension != null) {
        // TODO(tauno): not sure that's the right call here to fall back to the android extension
        return androidExtension.getTestOptions().getExecution().equalsIgnoreCase("ANDROIDX_TEST_ORCHESTRATOR");
      } else {
        return false;
      }
    })));
    deviceTestRunInput.getUseOrchestrator().disallowChanges();

    deviceTestRunInput.getClearPackageData().set(ext.getClearPackageData());
    deviceTestRunInput.getClearPackageData().disallowChanges();

    deviceTestRunInput.getWithCoverage().set(ext.getWithCoverage());
    deviceTestRunInput.getWithCoverage().disallowChanges();

    deviceTestRunInput.getAdditionalApks().set(ext.getAdditionalApks());
    deviceTestRunInput.getAdditionalApks().disallowChanges();

    deviceTestRunInput.getNumUniformShards().set(ext.getNumUniformShards());
    deviceTestRunInput.getNumUniformShards().disallowChanges();

    deviceTestRunInput.getNumBalancedShards().set(ext.getNumBalancedShards());
    deviceTestRunInput.getNumBalancedShards().disallowChanges();

    deviceTestRunInput.getNumShards().set(ext.getNumShards());
    deviceTestRunInput.getNumShards().disallowChanges();

    deviceTestRunInput.getShardTargetRuntime().set(ext.getShardTargetRuntime());
    deviceTestRunInput.getShardTargetRuntime().disallowChanges();

    deviceTestRunInput.getDirectoriesToPull().set(ext.getDirectoriesToPull());
    deviceTestRunInput.getDirectoriesToPull().disallowChanges();

    deviceTestRunInput.getSideEffects().set(ext.getSideEffects());
    deviceTestRunInput.getSideEffects().disallowChanges();

    deviceTestRunInput.getTestTimeout().set(ext.getTimeout());
    deviceTestRunInput.getTestTimeout().disallowChanges();

    deviceTestRunInput.getFileCacheEnabled().set(ext.getFileCacheEnabled());
    deviceTestRunInput.getFileCacheEnabled().disallowChanges();

    deviceTestRunInput.getFileCacheTtl().set(ext.getFileCacheTtl());
    deviceTestRunInput.getFileCacheTtl().disallowChanges();

    deviceTestRunInput.getTestCacheEnabled().set(ext.getTestCacheEnabled());
    deviceTestRunInput.getTestCacheEnabled().disallowChanges();

    deviceTestRunInput.getNumFlakyTestAttempts().set(ext.getNumFlakyTestAttempts());
    deviceTestRunInput.getNumFlakyTestAttempts().disallowChanges();

    deviceTestRunInput.getFlakyTestRepeatMode().set(ext.getFlakyTestRepeatMode());
    deviceTestRunInput.getFlakyTestRepeatMode().disallowChanges();

    deviceTestRunInput.getDisplayName().set(ext.getDisplayName());
    deviceTestRunInput.getDisplayName().disallowChanges();

    deviceTestRunInput.getDnsOverrides().set(ext.getDnsOverrides());
    deviceTestRunInput.getDnsOverrides().disallowChanges();

    deviceTestRunInput.getDnsServers().set(ext.getDnsServers());
    deviceTestRunInput.getDnsServers().disallowChanges();

    deviceTestRunInput.getEgressTunnel().set(ext.getEgressTunnel());
    deviceTestRunInput.getEgressTunnel().disallowChanges();

    deviceTestRunInput.getEgressLocalhostForwardIp().set(ext.getEgressLocalhostForwardIp());
    deviceTestRunInput.getEgressLocalhostForwardIp().disallowChanges();

    deviceTestRunInput.getRelays().set(ext.getRelays());
    deviceTestRunInput.getRelays().disallowChanges();

    deviceTestRunInput.getScmUrl().set(ext.getScmUrl());
    deviceTestRunInput.getScmUrl().disallowChanges();

    deviceTestRunInput.getScmCommitHash().set(ext.getScmCommitHash());
    deviceTestRunInput.getScmCommitHash().disallowChanges();

    deviceTestRunInput.getScmRefName().set(ext.getScmRefName());
    deviceTestRunInput.getScmRefName().disallowChanges();

    deviceTestRunInput.getScmPrUrl().set(ext.getScmPrUrl());
    deviceTestRunInput.getScmPrUrl().disallowChanges();

    deviceTestRunInput.getIgnoreFailures().set(ext.getIgnoreFailures());
    deviceTestRunInput.getIgnoreFailures().disallowChanges();

    deviceTestRunInput.getAsync().set(false);
    deviceTestRunInput.getAsync().disallowChanges();

    deviceTestRunInput.getPrintOutput().set(ext.getPrintOutput());
    deviceTestRunInput.getPrintOutput().disallowChanges();

    deviceTestRunInput.getTestTargets().set(ext.getTestTargets());
    deviceTestRunInput.getTestTargets().disallowChanges();

    deviceTestRunInput.getProxyHost().set(ext.getProxyHost());
    deviceTestRunInput.getProxyHost().disallowChanges();

    deviceTestRunInput.getProxyPort().set(ext.getProxyPort());
    deviceTestRunInput.getProxyPort().disallowChanges();

    deviceTestRunInput.getProxyUser().set(ext.getProxyUser());
    deviceTestRunInput.getProxyUser().disallowChanges();

    deviceTestRunInput.getProxyPassword().set(ext.getProxyPassword());
    deviceTestRunInput.getProxyPassword().disallowChanges();

    deviceTestRunInput.getNonProxyHosts().set(ext.getNonProxyHosts());
    deviceTestRunInput.getNonProxyHosts().disallowChanges();

    deviceTestRunInput.getClasspath().set(getProject().getConfigurations().named(ProjectConfigurator.TOOL_CONFIGURATION));
    deviceTestRunInput.getClasspath().disallowChanges();

    File intermediateFolder = getProject().getBuildDir().toPath().resolve("intermediates").resolve("emulatorwtf").toFile();
    deviceTestRunInput.getIntermediatesOutputs().set(intermediateFolder);
    deviceTestRunInput.getIntermediatesOutputs().disallowChanges();

    return deviceTestRunInput;
  }

  @Nullable
  private BaseExtension getAndroidExtension(Project project) {
    PluginContainer plugins = project.getPlugins();
    ExtensionContainer extensions = project.getExtensions();
    if (plugins.hasPlugin("com.android.application")) {
      return extensions.getByType(AppExtension.class);
    } else if (plugins.hasPlugin("com.android.library")) {
      return extensions.getByType(LibraryExtension.class);
    } else if (plugins.hasPlugin("com.android.test")) {
      return extensions.getByType(TestExtension.class);
    }
    return null;
  }
}
