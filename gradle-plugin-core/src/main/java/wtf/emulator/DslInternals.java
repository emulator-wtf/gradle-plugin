package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;

public class DslInternals {
  public static Action<EwVariantFilter> getFilter(EwInvokeDsl dsl) {
    return dsl.getFilterOrDefault();
  }

  public static DomainObjectSet<EwDeviceSpec> getDevices(EwInvokeDsl dsl) {
    return dsl.getDevicesOrDefault();
  }
}
