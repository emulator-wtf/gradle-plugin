plugins {
  alias(libs.plugins.android) apply false
  id("wtf.emulator.gradle")
}

dependencies {
  emulatorWtf(project(":app"))
}

emulatorwtf {
  version.set("1.0.0-SNAPSHOT")
}
