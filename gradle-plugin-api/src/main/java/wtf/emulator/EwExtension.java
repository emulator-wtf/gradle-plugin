package wtf.emulator;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class EwExtension extends EwInvokeDsl {
  private final Property<Integer> variantCount;

  public abstract Property<Boolean> getRepositoryCheckEnabled();

  public abstract Property<String> getVersion();

  public abstract Property<String> getToken();

  public abstract DirectoryProperty getBaseOutputDir();

  @Inject
  public EwExtension(ObjectFactory objectFactory) {
    this.variantCount = objectFactory.property(Integer.class).convention(0);
  }

  protected Property<Integer> getVariantCount() {
    return this.variantCount;
  }
}
