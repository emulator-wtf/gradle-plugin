plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
}

dependencies {
  api(projects.gradlePluginApi)

  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.kotlin.stdlib)
  compileOnly(pinned.agp.api.v81)
  compileOnly(pinned.builder.test.api)
  compileOnly(pinned.protobuf.java)

  compileOnly(libs.autovalue.annotations)

  api(libs.jsr305)

  annotationProcessor(libs.autovalue.compiler)
  annotationProcessor(libs.autovalue.gson.extension)
  annotationProcessor(libs.autovalue.gson.factory)

  implementation(projects.agpCompat)
  implementation(projects.gradleCompat)
  implementation(projects.gradlePluginProtos)
  implementation(projects.develocityReporter)

  implementation(libs.semver4j)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}
