/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  `kotlin-dsl`
}


group = "wtf.emulator.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  with(kotlinOptions) {
    jvmTarget = JavaVersion.VERSION_11.toString()
  }
}

dependencies {
  implementation(libs.gradle.plugins.publish)
}

gradlePlugin {
  plugins {
    register("kotlinConvention") {
      id = "wtf.emulator.java"
      implementationClass = "JavaConvention"
    }
//    register("binaryConvention") {
//      id = "wtf.emulator.binary"
//      implementationClass = "BinaryConvention"
//    }
//    register("kotlinJava11Convention") {
//      id = "wtf.emulator.kotlin.java11"
//      implementationClass = "KotlinJava11Convention"
//    }
  }
}
