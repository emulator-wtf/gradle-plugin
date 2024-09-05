plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v65)
  api(projects.gradleCompat61)

  api(projects.gradleCompatApi)
}
