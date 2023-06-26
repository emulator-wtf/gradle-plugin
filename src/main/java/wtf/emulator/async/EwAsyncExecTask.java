package wtf.emulator.async;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import wtf.emulator.EwExecTask;
import wtf.emulator.exec.EwWorkParameters;

import javax.inject.Inject;

public abstract class EwAsyncExecTask extends EwExecTask  {
  @Internal
  public abstract Property<EwAsyncExecService> getExecService();

  @Inject
  public abstract ObjectFactory getObjectFactory();

  @Override
  public void runTests() {
    EwWorkParameters params = getObjectFactory().newInstance(EwWorkParameters.class);
    fillWorkParameters(params);
    getExecService().get().executeAsync(params);
  }
}
