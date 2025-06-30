package wtf.emulator;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.testing.GroupTestEventReporter;
import org.gradle.api.tasks.testing.TestEventReporter;
import org.gradle.api.tasks.testing.TestOutputEvent;
import wtf.emulator.junit.JUnitResults;
import wtf.emulator.junit.JUnitTestCase;
import wtf.emulator.junit.JUnitTestSuite;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_8_13 implements GradleCompat {

  @Override
  public void reportTestResults(ObjectFactory objects, JUnitResults junitResults, @Nullable String resultsUrl) {
    TestReportingDepsHolder deps = objects.newInstance(TestReportingDepsHolder.class);
    try (GroupTestEventReporter rootReporter = deps.getTestEventReporterFactory().createTestEventReporter(
      "emulator.wtf",
      deps.getLayout().getBuildDirectory().dir("test-results/emulatorwtf-test").get(),
      deps.getLayout().getBuildDirectory().dir("reports/tests/emulatorwtf-test").get()
    )) {
      // Our junit results don't have a timestamp at the moment, so we use the current time.
      Instant reportingStartTime = Instant.now();
      rootReporter.started(reportingStartTime);

      if (resultsUrl != null) {
        rootReporter.metadata(reportingStartTime, "emulator.wtf URL", resultsUrl);
      }

      reportTestSuites(junitResults.getTestSuites(), rootReporter, reportingStartTime);

      Instant rootTotalTime = calculateEndTime(reportingStartTime, () ->
        junitResults.getTestSuites().stream()
          .mapToDouble(JUnitTestSuite::getTime)
          .sum()
      );
      if (junitResults.getTestSuites().stream().anyMatch(s -> s.getErrors() > 0 || s.getFailures() > 0)) {
        rootReporter.failed(rootTotalTime);
      } else {
        rootReporter.succeeded(rootTotalTime);
      }
    }

  }

  private static void reportTestSuites(List<JUnitTestSuite> testSuites, GroupTestEventReporter rootReporter, Instant reportingStartTime) {
    testSuites.forEach(testSuite -> {
      try (GroupTestEventReporter suiteReporter = rootReporter.reportTestGroup(testSuite.getName())) {
        suiteReporter.started(reportingStartTime);

        Map<String, List<JUnitTestCase>> testCasesByClass = testSuite.getTestcases().stream()
          .collect(Collectors.groupingBy(JUnitTestCase::getClassname));

        reportTestClasses(testCasesByClass, suiteReporter, reportingStartTime);

        Instant suiteTotalTime = calculateEndTime(reportingStartTime, testSuite::getTime);
        if (testSuite.getErrors() != 0 || testSuite.getFailures() != 0) {
          suiteReporter.failed(suiteTotalTime);
        } else {
          suiteReporter.succeeded(suiteTotalTime);
        }
      }
    });
  }

  private static void reportTestClasses(Map<String, List<JUnitTestCase>> testCasesByClass, GroupTestEventReporter suiteReporter, Instant reportingStartTime) {
    testCasesByClass.forEach((clazz, testCases) -> {
      try (GroupTestEventReporter classReporter = suiteReporter.reportTestGroup(clazz)) {
        classReporter.started(reportingStartTime);

        reportTestCases(testCases, classReporter, reportingStartTime);

        Instant classTotalTime = calculateEndTime(reportingStartTime, () -> testCases.stream()
          .mapToDouble(JUnitTestCase::getTime)
          .sum()
        );

        if (testCases.stream().anyMatch(testCase -> !testCase.getFailures().isEmpty() && testCase.getFlaky() != Boolean.TRUE)) {
          classReporter.failed(classTotalTime);
        } else {
          classReporter.succeeded(classTotalTime);
        }
      }
    });
  }

  private static void reportTestCases(List<JUnitTestCase> testCases, GroupTestEventReporter classReporter, Instant reportingStartTime) {
    testCases.forEach(testCase -> {
      Instant endTime = calculateEndTime(reportingStartTime, testCase::getTime);
      if (testCase.getFlaky() == Boolean.TRUE) {
        reportFlakyTestCase(testCase, classReporter, reportingStartTime, endTime);
      } else {
        reportNonFlakyTestCase(testCase, classReporter, reportingStartTime, endTime);
      }
    });
  }

  private static void reportFlakyTestCase(JUnitTestCase testCase, GroupTestEventReporter classReporter, Instant reportingStartTime, Instant endTime) {
    testCase.getFailures().forEach(failure -> {
      try (TestEventReporter test = createTestCaseReporter(classReporter, testCase)) {
        test.started(reportingStartTime);
        test.output(endTime, TestOutputEvent.Destination.StdErr, failure);
        test.failed(endTime, failure);
      }
    });

    try (TestEventReporter test = createTestCaseReporter(classReporter, testCase)) {
      test.started(reportingStartTime);
      test.succeeded(endTime);
    }
  }

  private static void reportNonFlakyTestCase(JUnitTestCase testCase, GroupTestEventReporter classReporter, Instant reportingStartTime, Instant endTime) {
    try (TestEventReporter test = createTestCaseReporter(classReporter, testCase)) {
      test.started(reportingStartTime);
      if (testCase.getFailures().isEmpty() && testCase.getSkipped() == null) {
        test.succeeded(endTime);
      } else if (testCase.getSkipped() != null) {
        test.skipped(endTime);
      } else {
        // Failed tests might have re-runs due to flaky tests. Report only the first failure.
        test.output(endTime, TestOutputEvent.Destination.StdErr, testCase.getFailures().get(0));
        test.failed(endTime, testCase.getFailures().get(0));
      }
    }
  }

  private static Instant calculateEndTime(Instant startTime, DoubleSupplier durationInSecondsCalculator) {
    return startTime.plus(Duration.ofMillis((long) (durationInSecondsCalculator.getAsDouble() * 1000)));
  }

  private static TestEventReporter createTestCaseReporter(GroupTestEventReporter groupReporter, JUnitTestCase testCase) {
    return groupReporter.reportTest(testCase.getClassname() + "#" + testCase.getName(), testCase.getName());
  }

}
