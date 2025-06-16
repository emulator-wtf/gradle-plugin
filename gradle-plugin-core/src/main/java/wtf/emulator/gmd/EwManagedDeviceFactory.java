package wtf.emulator.gmd;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class EwManagedDeviceFactory implements NamedDomainObjectFactory<EwManagedDevice> {

  private final ObjectFactory objectFactory;

  @Inject
  public EwManagedDeviceFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  @Override
  public EwManagedDevice create(String name) {
    return this.objectFactory.newInstance(EwManagedDeviceImpl.class, name);
  }
}
