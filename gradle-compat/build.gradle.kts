plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:6.1.1")

  api(projects.gradleCompatApi)

  implementation(projects.gradleCompat61)
  implementation(projects.gradleCompat66)
  implementation(projects.gradleCompat76)

  implementation("com.vdurmont:semver4j:3.1.0")
}
