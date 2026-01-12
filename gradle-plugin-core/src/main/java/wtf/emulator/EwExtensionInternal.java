package wtf.emulator;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

public class EwExtensionInternal {
  private final EwExtension extension;

  public EwExtensionInternal(EwExtension extension) {
    this.extension = extension;
  }

  public Action<EwVariantFilter> getFilter() {
    return extension.getFilter();
  }

  public Property<Integer> getVariantCount() {
    return extension.getVariantCount();
  }
}
