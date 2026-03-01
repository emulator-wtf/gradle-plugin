plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
}

dependencies {
  compileOnly(pinned.agp.api.v81)
  compileOnly(pinned.gradle.api.v80)
  compileOnly(libs.develocity)

  api(libs.jsr305)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}
