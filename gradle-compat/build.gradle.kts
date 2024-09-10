plugins {
  id("wtf.emulator.java")
}

dependencies {
  compileOnly(libs.gradle.api.v61)

  api(projects.gradleCompatApi)

  implementation(projects.gradleCompat61)
  implementation(projects.gradleCompat65)
  implementation(projects.gradleCompat66)
  implementation(projects.gradleCompat68)
  implementation(projects.gradleCompat74)

  implementation(libs.semver4j)
}

lint {
  // TODO(madis): these two trigger for various dev.gradle.plugins:gradle-api:* for some reason,
  //              but why in this project? need to investigate
  disable.add("GradleDependency")
  disable.add("SimilarGradleDependency")
}
