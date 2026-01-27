package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class EwInvokeDsl implements EwInvokeConfiguration {
  public abstract Property<String> getTestRunnerClass();

  public abstract MapProperty<String, Object> getEnvironmentVariables();

  public abstract MapProperty<String, Object> getSecretEnvironmentVariables();

  public abstract ListProperty<TestReporter> getTestReporters();

  public abstract DomainObjectSet<EwDeviceSpec> getDevices();

  /**
   * Run only a subset of matching test targets, these will be forwarded to {@code AndroidJUnitRunner}.
   * See the full list of configuration options
   * <a href="https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#typical-usage">here</a>.
   * NOTE: prefer using the {@link #targets(Action)} method instead unless you specifically need the lazyness
   * of a {@link org.gradle.api.provider.Property}.
   */
  public abstract Property<TestTargetsSpec> getTestTargets();

  @Inject
  protected abstract ObjectFactory getObjectFactory();

  private Action<EwVariantFilter> filter = null;

  @SuppressWarnings("unused")
  public void variantFilter(Action<EwVariantFilter> filter) {
    this.filter = filter;
  }

  @SuppressWarnings("unused")
  public void device(Action<EwDeviceSpec> action) {
    EwDeviceSpec builder = getObjectFactory().newInstance(EwDeviceSpec.class);
    action.execute(builder);
    getDevices().add(builder);
  }

  @SuppressWarnings("unused")
  public void targets(Action<TestTargetsSpec> action) {
    final var obj = getObjectFactory().newInstance(TestTargetsSpec.class);
    action.execute(obj);
    getTestTargets().set(obj);
  }

  protected DomainObjectSet<EwDeviceSpec> getDevicesOrDefault() {
    return getDevices();
  }

  protected Action<EwVariantFilter> getFilter() {
    return this.filter;
  }

  protected Action<EwVariantFilter> getFilterOrDefault() {
    return this.filter;
  }
}
