plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.agp.api.v85)
  compileOnly(pinned.kotlin.stdlib)

  api(projects.agpCompatApi)
}
