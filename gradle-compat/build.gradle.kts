plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v61)

  api(projects.gradleCompatApi)

  implementation(projects.gradleCompat61)
  implementation(projects.gradleCompat66)
  implementation(projects.gradleCompat76)

  implementation(libs.semver4j)
}
