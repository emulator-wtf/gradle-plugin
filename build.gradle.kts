plugins {
  `java-gradle-plugin`
  id("wtf.emulator.java")

  alias(libs.plugins.buildconfig)
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
  implementation(libs.semver4j)
}

buildConfig {
  packageName("wtf.emulator")
  buildConfigField("String", "VERSION", "\"${project.findProperty("VERSION_NAME")?.toString() ?: project.version}\"")
}
