plugins {
  alias(libs.plugins.android.library)
  id("wtf.emulator.gradle")
}

android {
  compileSdk = 34

  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableAndroidTestCoverage = false
    }
  }

  namespace = "wtf.emulator.sample.library"
}

dependencies {
  androidTestImplementation("wtf.emulator:test-runtime-android:0.2.0")
  androidTestImplementation("androidx.test:rules:1.6.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
  androidTestImplementation("androidx.test:core:1.6.1")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
  androidTestImplementation("com.google.truth:truth:1.4.4")
}
