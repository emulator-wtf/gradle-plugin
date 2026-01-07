package wtf.emulator;

import com.android.build.api.variant.Variant;

public class EwVariantFilter {
  private final Variant variant;
  private boolean enabled = true;

  public EwVariantFilter(Variant variant) {
    this.variant = variant;
  }

  @SuppressWarnings("unused")
  public Variant getVariant() {
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
