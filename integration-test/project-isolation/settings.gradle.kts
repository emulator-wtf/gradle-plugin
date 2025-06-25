pluginManagement {
  repositories {
    maven(File(File(rootDir.parentFile.parentFile, "build"), "maven-repo").toURI())
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  plugins {
    id("wtf.emulator.gradle") version "+"
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven("https://maven.emulator.wtf/releases/") {
      content {
        includeGroup("wtf.emulator")
      }
    }
    google()
    mavenCentral()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "project-isolation"

include(":app")
include(":library")
include(":test")
include(":baselineprofile")
includeBuild("../test-conventions-plugin")
