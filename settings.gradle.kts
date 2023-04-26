pluginManagement {
  includeBuild("build-logic")
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.vanniktech.maven.publish") {
        useModule("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
      }
    }
  }
  repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

include(":gradle-compat-api")
include(":gradle-compat")
include(":gradle-compat-6-1")
include(":gradle-compat-6-6")
include(":gradle-compat-7-6")

rootProject.name = "gradle-plugin"