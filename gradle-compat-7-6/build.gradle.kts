plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:7.6")

  api(projects.gradleCompatApi)
}
