plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.android.kotlin.multiplatform.library) apply false
  alias(libs.plugins.emulatorwtf)
}

dependencies {
  emulatorwtf(projects.app)
}
