package wtf.emulator;

import com.android.build.gradle.api.BaseVariant;

public class EwVariantFilter {
  private final BaseVariant variant;
  private boolean enabled = true;

  protected EwVariantFilter(BaseVariant variant) {
    this.variant = variant;
  }

  @SuppressWarnings("unused")
  public BaseVariant getVariant() {
    return variant;
  }

  @SuppressWarnings("unused")
  public boolean isEnabled() {
    return enabled;
  }

  @SuppressWarnings("unused")
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
