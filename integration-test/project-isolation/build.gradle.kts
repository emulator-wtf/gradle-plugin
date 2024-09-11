plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.test) apply false
  id("wtf.emulator.gradle")
}

dependencies {
  emulatorwtf(projects.app)
  emulatorwtf(projects.library)
  emulatorwtf(projects.test)
}
