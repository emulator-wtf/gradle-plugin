plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
}

dependencies {
  api(projects.gradlePluginApi)

  compileOnly(libs.gradle.api.v81)
  compileOnly(libs.kotlin.stdlib)

  compileOnly(libs.agp)
  compileOnly(libs.builder.test.api)
  compileOnly(libs.autovalue.annotations)
  compileOnly(libs.testing.platfomr.core.proto)

  api(libs.jsr305)

  annotationProcessor(libs.autovalue.compiler)
  annotationProcessor(libs.autovalue.gson.extension)
  annotationProcessor(libs.autovalue.gson.factory)

  implementation(projects.gradleCompat)
  implementation(projects.develocityReporter)

  implementation(libs.semver4j)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}
