plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.agp.api.v81)

  compileOnly(libs.autovalue.annotations)

  annotationProcessor(libs.autovalue.compiler)
}
