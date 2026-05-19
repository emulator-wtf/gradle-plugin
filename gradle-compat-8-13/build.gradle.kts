plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.gradle.api.v813)

  api(projects.gradleCompatApi)
}
