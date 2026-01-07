package wtf.emulator;

import org.gradle.api.model.ObjectFactory;
import wtf.emulator.junit.JUnitResults;

import javax.annotation.Nullable;

public interface GradleCompat {
  void reportTestResults(ObjectFactory objects, JUnitResults junitResults, @Nullable String resultsUrl);
}
