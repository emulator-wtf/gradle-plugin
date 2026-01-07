plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.agp.impl.v81)

  api(projects.agpCompatApi)
}
