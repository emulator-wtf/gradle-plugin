plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  compileOnly(pinned.agp.api.v81)
  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.kotlin.stdlib)

  implementation(projects.gradlePluginApi)
}
