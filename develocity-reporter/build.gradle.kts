plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
}

dependencies {
  compileOnly(libs.agp.api.v81)
  compileOnly(libs.develocity)
  compileOnly(libs.gradle.api.v80)

  api(libs.jsr305)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}
