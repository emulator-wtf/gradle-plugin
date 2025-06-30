package wtf.emulator.gmd;

import com.android.build.api.instrumentation.manageddevice.DeviceSetupConfigureAction;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.jetbrains.annotations.NotNull;
import wtf.emulator.EwExtension;

import javax.inject.Inject;

public abstract class EwDeviceSetupConfigureAction implements DeviceSetupConfigureAction<EwManagedDevice, EwDeviceSetupInput> {

  @Inject
  public abstract ObjectFactory getObjectFactory();

  @Inject
  public abstract Project getProject();

  @NotNull
  @Override
  public EwDeviceSetupInput configureTaskInput(@NotNull EwManagedDevice ewManagedDevice) {
    EwDeviceSetupInput deviceSetupInput = getObjectFactory().newInstance(EwDeviceSetupInput.class);
    EwExtension ext = getProject().getExtensions().getByType(EwExtension.class);

    // If the test invocation has side effects or the test cache is not enabled, we'll mark the task cache as disabled.
    deviceSetupInput.getCacheEnabled().set(ext.getSideEffects().getOrElse(false).booleanValue()
      || !ext.getTestCacheEnabled().getOrElse(true).booleanValue());
    deviceSetupInput.getCacheEnabled().disallowChanges();
    return deviceSetupInput;
  }
}
