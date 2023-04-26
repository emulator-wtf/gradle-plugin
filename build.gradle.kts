plugins {
  `java-gradle-plugin`
  id("wtf.emulator.java")
  id("com.github.gmazzo.buildconfig") version "4.0.2"
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
  compileOnly("com.android.tools.build:gradle:4.0.0")

  implementation(projects.gradleCompat)
  implementation("com.vdurmont:semver4j:3.1.0")
}

buildConfig {
  packageName("wtf.emulator")
  buildConfigField("String", "VERSION", "\"${project.findProperty("VERSION_NAME")?.toString() ?: project.version}\"")
}
