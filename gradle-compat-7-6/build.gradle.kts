plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v76)
  api(projects.gradleCompat74)

  api(projects.gradleCompatApi)
}
