package wtf.emulator.gmd;

import com.android.build.api.dsl.Device;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import wtf.emulator.DeviceModel;
import wtf.emulator.GpuMode;

public interface EwManagedDevice extends Device {

  @Optional
  @Input
  Property<DeviceModel> getDevice();

  @Optional
  @Input
  Property<Integer> getApiLevel();

  @Optional
  @Input
  Property<GpuMode> getGpu();

}
