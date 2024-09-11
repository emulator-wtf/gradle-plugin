plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
}

dependencies {
  api(projects.gradlePluginApi)

  compileOnly(libs.gradle.api.v70)

  compileOnly(libs.agp)
  compileOnly(libs.autovalue.annotations)

  api(libs.jsr305)

  annotationProcessor(libs.autovalue.compiler)
  annotationProcessor(libs.autovalue.gson.extension)
  annotationProcessor(libs.autovalue.gson.factory)

  implementation(projects.gradleCompat)
  implementation(libs.semver4j)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}
