plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  compileOnly(libs.agp)
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.kotlin.stdlib)

  implementation(projects.gradlePluginApi)
}
