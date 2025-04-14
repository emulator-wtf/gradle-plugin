package wtf.emulator.junit;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

public class JUnitResultsBuilder {
  private final List<JUnitTestSuite> testSuites = new ArrayList<>();
  private List<JUnitTestCase> curTestSuite;

  private JUnitTestCase curTestCase;
  private List<String> curTestCaseFailures = new ArrayList<>();

  public void visitTestsuite(Attributes attributes) {
    curTestSuite = new ArrayList<>();
    String name = attributes.getValue("name");
    if (name == null) {
      name = "";
    }
    int tests = parseInt(attributes.getValue("tests"));
    int failures = parseInt(attributes.getValue("failures"));
    int flakes = parseInt(attributes.getValue("flakes"));
    int errors = parseInt(attributes.getValue("errors"));
    int skipped = parseInt(attributes.getValue("skipped"));
    double time = parseDouble(attributes.getValue("time"));
    String hostname = attributes.getValue("hostname");
    if (hostname == null) {
      hostname = "localhost";
    }
    JUnitTestSuite suite = new JUnitTestSuite(name, tests, failures, flakes, errors, skipped, time, hostname, curTestSuite);
    testSuites.add(suite);
  }

  public void visitTestcaseStart(Attributes attributes) {
    curTestCaseFailures = new ArrayList<>();
    String name = attributes.getValue("name");
    if (name == null) {
      name = "";
    }
    String classname = attributes.getValue("classname");
    if (classname == null) {
      classname = "";
    }
    double time = parseDouble(attributes.getValue("time"));
    Boolean flaky = null;
    String flakyValue = attributes.getValue("flaky");
    if (flakyValue != null) {
      flaky = Boolean.parseBoolean(flakyValue);
    }
    curTestCase = new JUnitTestCase(name, classname, time, flaky, null, curTestCaseFailures);
  }

  public void visitTestcaseEnd() {
    curTestSuite.add(curTestCase);
  }

  public void visitFailure(String text) {
    curTestCaseFailures.add(text);
  }

  public void visitSkipped(String text) {
    curTestCase = new JUnitTestCase(
      curTestCase.getName(),
      curTestCase.getClassname(),
      curTestCase.getTime(),
      curTestCase.getFlaky(),
      text,
      curTestCase.getFailures()
    );
  }

  public JUnitResults build() {
    return new JUnitResults(testSuites);
  }

  private int parseInt(String s) {
    if (s == null) {
      return 0;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private double parseDouble(String s) {
    if (s == null) {
      return 0.0;
    }
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }
}
