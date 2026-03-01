plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(pinned.agp.api.v81)

  api(projects.agpCompatApi)

  implementation(projects.agpCompat81)
  implementation(projects.agpCompat85)
}
