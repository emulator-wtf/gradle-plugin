plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.gradle.api.v94) {
    capabilities {
      requireCapability("org.gradle.experimental:gradle-public-api-internal")
    }
  }

  api(projects.gradleCompatApi)
}
