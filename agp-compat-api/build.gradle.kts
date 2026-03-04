plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.agp.api.v81)

  compileOnly(libs.autovalue.annotations)

  annotationProcessor(libs.autovalue.compiler)
}
