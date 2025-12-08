plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v80)

  api(projects.gradleCompatApi)

  implementation(projects.gradleCompat80)
  implementation(projects.gradleCompat813)

  implementation(libs.semver4j)
}

lint {
  // TODO(madis): these two trigger for various dev.gradle.plugins:gradle-api:* for some reason,
  //              but why in this project? need to investigate
  disable.add("GradleDependency")
  disable.add("SimilarGradleDependency")
}
