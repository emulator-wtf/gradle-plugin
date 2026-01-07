plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.agp.api.v85)
  compileOnly(libs.kotlin.stdlib)

  api(projects.agpCompatApi)
}
