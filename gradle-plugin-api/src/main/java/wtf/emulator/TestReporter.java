package wtf.emulator;

public enum TestReporter {
  /**
   * Import result JUnit XML files to Develocity using the
   * <a href="https://docs.gradle.com/enterprise/gradle-plugin/api/com/gradle/develocity/agent/gradle/test/ImportJUnitXmlReports.html">ImportJUnitXmlReports task</a>
   */
  DEVELOCITY,

  /**
   * Report test events to Gradle using the <a href="https://docs.gradle.org/current/userguide/test_reporting_api.html">Gradle test reporting API</a>
   */
  GRADLE_TEST_REPORTING_API
}
