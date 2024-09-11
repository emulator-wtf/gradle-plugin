plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v70)

  api(projects.gradleCompatApi)
}
