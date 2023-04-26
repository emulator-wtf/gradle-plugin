plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:6.6")

  api(projects.gradleCompatApi)
}
