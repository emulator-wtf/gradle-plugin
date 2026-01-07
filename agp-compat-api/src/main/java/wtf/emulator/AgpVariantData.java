package wtf.emulator;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.io.Serializable;

public abstract class AgpVariantData implements Serializable {
  public abstract Property<Boolean> getTestCoverageEnabled();

  public abstract MapProperty<String, String> getInstrumentationRunnerArguments();
}
