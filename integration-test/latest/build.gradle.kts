import org.gradle.api.internal.tasks.DefaultTaskDependency

plugins {
  alias(libs.plugins.android.application)
  id("wtf.emulator.gradle")
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

android {
  compileSdk = 33

  defaultConfig {
    applicationId = "wtf.emulator.sample"
    minSdk = 23
    targetSdk = 33
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

dependencies {
  androidTestImplementation("wtf.emulator:test-runtime-android:0.2.1")
  androidTestImplementation("androidx.test:rules:1.7.0")
  androidTestImplementation("androidx.test:runner:1.6.2")
  androidTestImplementation("androidx.test:core:1.7.0")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
  androidTestImplementation("com.google.truth:truth:1.4.4")
}
