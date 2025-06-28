package wtf.emulator.gmd;

import com.android.build.api.instrumentation.manageddevice.DeviceSetupInput;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public abstract class EwDeviceSetupInput implements DeviceSetupInput {

  @Input
  public abstract Property<Boolean> getCacheEnabled();

}
