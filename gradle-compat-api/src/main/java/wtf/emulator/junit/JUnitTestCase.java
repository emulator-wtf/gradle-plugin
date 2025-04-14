package wtf.emulator.junit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class JUnitTestCase {
  private final String name;
  private final String classname;
  private final double time;
  @Nullable
  private final Boolean flaky;
  @Nullable
  private final String skipped;
  private final List<String> failures;

  public JUnitTestCase(String name, String classname, double time,
                       @Nullable Boolean flaky, @Nullable String skipped, List<String> failures) {
    this.name = name;
    this.classname = classname;
    this.time = time;
    this.flaky = flaky;
    this.skipped = skipped;
    this.failures = failures;
  }

  public String getName() {
    return name;
  }

  public String getClassname() {
    return classname;
  }

  public double getTime() {
    return time;
  }

  @Nullable
  public Boolean getFlaky() {
    return flaky;
  }

  @Nullable
  public String getSkipped() {
    return skipped;
  }

  public List<String> getFailures() {
    return failures;
  }

  public JUnitTestCase plus(JUnitTestCase other) {
    if (!name.equals(other.name) || !classname.equals(other.classname)) {
      throw new IllegalArgumentException("cannot merge tests with different names");
    }
    double newTime = Math.max(time, other.time);
    boolean candidate = Boolean.TRUE.equals(flaky) ||
      Boolean.TRUE.equals(other.flaky) ||
      (failures.isEmpty() != other.failures.isEmpty());
    Boolean newFlaky = candidate ? Boolean.TRUE : null;
    String newSkipped = (skipped == null || other.skipped == null) ? null : skipped;
    List<String> newFailures = new ArrayList<>(failures);
    newFailures.addAll(other.failures);
    return new JUnitTestCase(name, classname, newTime, newFlaky, newSkipped, newFailures);
  }

  public JUnitTestCase withTime(double newTime) {
    return new JUnitTestCase(name, classname, newTime, flaky, skipped, failures);
  }
}
