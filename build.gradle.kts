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
  compileOnly(libs.agp)

  implementation(projects.gradleCompat)
  implementation(libs.semver4j)
  implementation(libs.json)
  implementation(libs.commons.io)
}

buildConfig {
  packageName("wtf.emulator")
  buildConfigField("String", "VERSION", "\"${providers.gradleProperty("VERSION_NAME").orNull ?: project.version}\"")
}
