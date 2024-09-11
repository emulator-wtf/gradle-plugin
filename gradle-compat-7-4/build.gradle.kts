plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v74)
  api(projects.gradleCompat70)

  api(projects.gradleCompatApi)
}
