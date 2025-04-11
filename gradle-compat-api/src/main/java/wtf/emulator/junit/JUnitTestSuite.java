package wtf.emulator.junit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JUnitTestSuite {
  private final String name;
  private final int tests;
  private final int failures;
  private final int flakes;
  private final int errors;
  private final int skipped;
  private final double time;
  private final String hostname;
  private final List<JUnitTestCase> testcases;

  public JUnitTestSuite(String name, int tests, int failures, int flakes, int errors, int skipped,
                        double time, String hostname, List<JUnitTestCase> testcases) {
    this.name = name;
    this.tests = tests;
    this.failures = failures;
    this.flakes = flakes;
    this.errors = errors;
    this.skipped = skipped;
    this.time = time;
    this.hostname = hostname;
    this.testcases = testcases;
  }

  public String getName() {
    return name;
  }

  public int getTests() {
    return tests;
  }

  public int getFailures() {
    return failures;
  }

  public int getFlakes() {
    return flakes;
  }

  public int getErrors() {
    return errors;
  }

  public int getSkipped() {
    return skipped;
  }

  public double getTime() {
    return time;
  }

  public String getHostname() {
    return hostname;
  }

  public List<JUnitTestCase> getTestcases() {
    return testcases;
  }

  public JUnitTestSuite plus(JUnitTestSuite suite) {
    if (!name.equals(suite.name)) {
      throw new IllegalArgumentException("cannot merge testsuites with different names");
    }
    List<JUnitTestCase> mergedTestcases = Stream.concat(testcases.stream(), suite.testcases.stream())
      .collect(Collectors.groupingBy(tc -> tc.getClassname() + "\0" + tc.getName()))
      .values().stream()
      .map(list -> list.stream().reduce(JUnitTestCase::plus).orElse(null))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    int newTests = mergedTestcases.size();
    int newFailures = (int) mergedTestcases.stream()
      .filter(tc -> !tc.getFailures().isEmpty() && !Boolean.TRUE.equals(tc.getFlaky()))
      .count();
    int newFlakes = (int) mergedTestcases.stream()
      .filter(tc -> Boolean.TRUE.equals(tc.getFlaky()))
      .count();
    int newErrors = 0;
    int newSkipped = (int) mergedTestcases.stream()
      .filter(tc -> tc.getSkipped() != null)
      .count();
    double newTime = Math.max(time, suite.time);
    return new JUnitTestSuite(name, newTests, newFailures, newFlakes, newErrors, newSkipped, newTime, hostname, mergedTestcases);
  }

  /**
   * Returns a new testsuite instance with a name suffixed appropriately.
   */
  public JUnitTestSuite withNameSuffix(String suffix) {
    String newName = name.isEmpty() ? suffix : name + " (" + suffix + ")";
    return new JUnitTestSuite(newName, tests, failures, flakes, errors, skipped, time, hostname, testcases);
  }

  /**
   * Returns a new testsuite with a different testcases list.
   */
  public JUnitTestSuite withTestcases(List<JUnitTestCase> newTestcases) {
    return new JUnitTestSuite(name, tests, failures, flakes, errors, skipped, time, hostname, newTestcases);
  }

}
