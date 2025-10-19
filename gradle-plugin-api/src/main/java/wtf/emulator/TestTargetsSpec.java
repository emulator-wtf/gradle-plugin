package wtf.emulator;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.io.Serializable;

/**
 * Specify which tests to run. The parameters here map closely to the {@code AndroidJUnitRunner} arguments.
 * See <a href="https://developer.android.com/reference/kotlin/androidx/test/runner/AndroidJUnitRunner?hl=en">AndroidJUnitRunner docs</a>
 * for more details.
 */
public abstract class TestTargetsSpec implements Serializable {
  /**
   * List of package names to include in the test run.
   */
  public abstract ListProperty<String> getPackages();

  /**
   * List of package names to exclude from the test run.
   */
  public abstract ListProperty<String> getExcludePackages();

  /**
   * List of test class names to include in the test run.
   */
  public abstract ListProperty<String> getClasses();

  /**
   * List of test class names to exclude from the test run.
   */
  public abstract ListProperty<String> getExcludeClasses();

  /**
   * List of test methods (optionally with parameters) to include in the test run.
   */
  public abstract ListProperty<TestTargetMethod> getMethods();

  /**
   * List of test methods (optionally with parameters) to exclude from the test run.
   */
  public abstract ListProperty<TestTargetMethod> getExcludeMethods();

  /**
   * List of annotations to include in the test run.
   */
  public abstract ListProperty<String> getAnnotations();

  /**
   * List of annotations to exclude from the test run.
   */
  public abstract ListProperty<String> getExcludeAnnotations();

  /**
   * List of filters to apply to the test run.
   * See <a href="https://junit.org/junit4/javadoc/4.12/org/junit/runner/manipulation/Filter.html">JUnit documentation on filters.</a>
   */
  public abstract ListProperty<String> getFilters();

  /**
   * Size of tests to include in the test run. This maps to the Android test size annotations, i.e.
   * {@code androidx.test.filters.SmallTest}, {@code androidx.test.filters.MediumTest} and {@code androidx.test.filters.LargeTest}.
   */
  public abstract Property<TestSize> getSize();

  /**
   * Regular expression to select tests to include in the test run.
   */
  public abstract Property<String> getRegex();
}
