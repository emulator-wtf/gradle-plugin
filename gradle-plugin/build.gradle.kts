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
  api(projects.kotlinExtensions)

  implementation(projects.gradlePluginCore)
}
