pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":gradle-compat-api")
include(":gradle-compat")
include(":gradle-compat-6-1")
include(":gradle-compat-6-6")
include(":gradle-compat-6-5")
include(":gradle-compat-7-4")
include(":gradle-compat-7-6")

rootProject.name = "gradle-plugin"
