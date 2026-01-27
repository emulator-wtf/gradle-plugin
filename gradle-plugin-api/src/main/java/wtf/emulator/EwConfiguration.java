package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;

public abstract class EwConfiguration extends EwInvokeDsl implements Named {
  private Action<EwVariantFilter> defaultFilter = null;
  private DomainObjectSet<EwDeviceSpec> defaultDevices = null;

  protected void setDefaultFilter(Action<EwVariantFilter> defaultFilter) {
    this.defaultFilter = defaultFilter;
  }

  protected void setDefaultDevices(DomainObjectSet<EwDeviceSpec> defaultDevices) {
    this.defaultDevices = defaultDevices;
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
