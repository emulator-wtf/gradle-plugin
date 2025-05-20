pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    google()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":develocity-reporter")

include(":gradle-compat-api")
include(":gradle-compat")
include(":gradle-compat-7-0")
include(":gradle-compat-7-4")
include(":gradle-compat-8-13")

include(":gradle-plugin")
include(":gradle-plugin-api")
include(":gradle-plugin-core")

rootProject.name = "ew-gradle"
