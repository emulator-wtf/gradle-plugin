plugins {
  alias(libs.plugins.android)
  id("wtf.emulator.gradle")
}

android {
  compileSdk = 34

  defaultConfig {
    applicationId = "wtf.emulator.sample"
    minSdk = 23
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableAndroidTestCoverage = false
    }
  }

  namespace = "wtf.emulator.sample"
}

emulatorwtf {
  version.set("1.0.0-SNAPSHOT")
  async.set(true)
}

dependencies {
  androidTestImplementation("wtf.emulator:test-runtime-android:0.2.0")
  androidTestImplementation("androidx.test:rules:1.6.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
  androidTestImplementation("androidx.test:core:1.6.1")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
  androidTestImplementation("com.google.truth:truth:1.4.4")

  testImplementation("junit:junit:4.13.2")
}
