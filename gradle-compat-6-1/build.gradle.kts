plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:6.1.1")

  api(projects.gradleCompatApi)
}
