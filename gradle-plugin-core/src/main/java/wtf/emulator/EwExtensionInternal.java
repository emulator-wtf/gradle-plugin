package wtf.emulator;

import org.gradle.api.provider.Property;

public class EwExtensionInternal {
  private final EwExtension extension;

  public EwExtensionInternal(EwExtension extension) {
    this.extension = extension;
  }

  public Property<Integer> getVariantCount() {
    return extension.getVariantCount();
  }
}
