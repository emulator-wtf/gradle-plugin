plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.test) apply false
  id("wtf.emulator.gradle")
  id("wtf.emulator.test.integration-test-conventions") apply false
}

dependencies {
  emulatorwtf(projects.app)
  emulatorwtf(projects.library)
  emulatorwtf(projects.test)
}
