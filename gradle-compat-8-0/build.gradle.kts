plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v80)

  api(projects.gradleCompatApi)
}
