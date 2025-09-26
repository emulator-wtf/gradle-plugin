package wtf.emulator;

import org.gradle.api.model.ObjectFactory;
import wtf.emulator.junit.JUnitResults;

import javax.annotation.Nullable;

public class GradleCompat_8_0 implements GradleCompat {

  @Override
  public void reportTestResults(ObjectFactory objects, JUnitResults junitResults, @Nullable String resultsUrl) {
    /* intentionally empty */
  }
}
