plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "2.3.0"
}

gradlePlugin {
  plugins {
    create("integrationTestConventions") {
      id = "wtf.emulator.test.integration-test-conventions"
      implementationClass = "wtf.emulator.test.IntegrationTestConventionsPlugin"
      displayName = "Shared Tasks for Integration Tests"
      description = "Provides common task definitions for integration test projects."
    }
  }
}

group = "wtf.emulator.test.convention"
version = "1.0"
