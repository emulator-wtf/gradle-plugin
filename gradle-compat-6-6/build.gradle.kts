plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v66)

  api(projects.gradleCompatApi)
}
