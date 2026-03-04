plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.agp.impl.v81)

  api(projects.agpCompatApi)
}
