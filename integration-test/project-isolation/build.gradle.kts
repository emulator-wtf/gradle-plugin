plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.test) apply false
  id("wtf.emulator.gradle")
}

dependencies {
  emulatorWtf(project(":app"))
  emulatorWtf(project(":library"))
  emulatorWtf(project(":test"))
}

emulatorwtf {
  version.set("1.0.0-SNAPSHOT")
}
