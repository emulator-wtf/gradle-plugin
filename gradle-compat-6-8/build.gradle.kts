plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v68)
  api(projects.gradleCompat66)

  api(projects.gradleCompatApi)
}
