plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(gradleApi())

  api(projects.gradleCompatApi)
}
