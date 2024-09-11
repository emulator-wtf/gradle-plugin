plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.test) apply false
  id("wtf.emulator.gradle")
}

dependencies {
  emulatorwtf(project(":app"))
  emulatorwtf(project(":library"))
  emulatorwtf(project(":test"))

  emulatorwtf {
    
  }
}

emulatorwtf {
//  devices = listOf()
}
