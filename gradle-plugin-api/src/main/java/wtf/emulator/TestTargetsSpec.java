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

  /**
   * Add classes in the package to include in the test run.
   */
  public void testPackage(String packageName) {
    getPackages().add(packageName);
  }

  /**
   * Exclude classes in the package from the test run.
   */
  public void excludeTestPackage(String packageName) {
    getExcludePackages().add(packageName);
  }

  /**
   * Add a test class to include in the test run.
   *
   * @param className fully qualified name (including package name) of the test class
   */
  public void testClass(String className) {
    getClasses().add(className);
  }

  /**
   * Exclude a test class from the test run.
   *
   * @param className fully qualified name (including package name) of the test class
   */
  public void excludeTestClass(String className) {
    getExcludeClasses().add(className);
  }

  /**
   * Add a test method to include in the test run.
   * @param className fully qualified name (including package name) of the test class
   * @param methodName name of the test method, optionally including parameters
   */
  public void testMethod(String className, String methodName) {
    getMethods().add(TestTargetMethod.create(className, methodName));
  }

  /**
   * Exclude a test method from the test run.
   * @param className fully qualified name (including package name) of the test class
   * @param methodName name of the test method, optionally including parameters
   */
  public void excludeTestMethod(String className, String methodName) {
    getExcludeMethods().add(TestTargetMethod.create(className, methodName));
  }

  /**
   * All test methods annotated with this annotation will be included in the test run.
   * @param annotationClassName fully qualified name (including package name) of the annotation class
   */
  public void annotation(String annotationClassName) {
    getAnnotations().add(annotationClassName);
  }

  /**
   * All test methods annotated with this annotation will be excluded from the test run.
   * @param annotation fully qualified name (including package name) of the annotation class
   */
  public void excludeAnnotation(String annotation) {
    getExcludeAnnotations().add(annotation);
  }

  /**
   * Add a filter to apply to the test run.
   * See <a href="https://junit.org/junit4/javadoc/4.12/org/junit/runner/manipulation/Filter.html">JUnit documentation on filters.</a>
   * @param filterClassName fully qualified name (including package name) of the filter class
   */
  public void filter(String filterClassName) {
    getFilters().add(filterClassName);
  }

  /**
   * Size of tests to include in the test run. This maps to the Android test size annotations, i.e.
   * {@code androidx.test.filters.SmallTest}, {@code androidx.test.filters.MediumTest} and {@code androidx.test.filters.LargeTest}.
   */
  public void size(TestSize size) {
    getSize().set(size);
  }
}
