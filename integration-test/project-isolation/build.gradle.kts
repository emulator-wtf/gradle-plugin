plugins {
  alias(libs.plugins.android) apply false
  id("wtf.emulator.gradle")
}

dependencies {
  emulatorWtf(project(":app"))
}
