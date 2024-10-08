plugins {
  `java-gradle-plugin`
  id("wtf.emulator.java")
}

gradlePlugin {
  plugins {
    create("ewPlugin") {
      id = "wtf.emulator.gradle"
      implementationClass = "wtf.emulator.EwPlugin"
    }
  }
}

dependencies {
  api(projects.gradlePluginApi)

  implementation(projects.gradleCompat)
  implementation(projects.gradlePluginCore)
}
