package wtf.emulator;

public enum TestReporter {
  /**
   * Import result JUnit XML files to Develocity using the
   * [ImportJUnitXmlReports task](https://docs.gradle.com/enterprise/gradle-plugin/api/com/gradle/develocity/agent/gradle/test/ImportJUnitXmlReports.html)
   */
  DEVELOCITY,

  /**
   * Report test events to Gradle using the [Gradle test reporting API](https://docs.gradle.org/current/userguide/test_reporting_api.html)
   */
  GRADLE_TEST_REPORTING_API
}
