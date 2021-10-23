plugins {
  `java-gradle-plugin`
  `maven-publish`
}

repositories {
  mavenCentral()
  google()
}

gradlePlugin {
  plugins {
    create("ewPlugin") {
      id = "wtf.emulator.gradle"
      implementationClass = "wtf.emulator.EwPlugin"
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  compileOnly("com.android.tools.build:gradle:4.0.0")
}

val tag = System.getenv()["PLUGIN_TAG"]
val pluginVersion = tag?.split('/')?.last()?.removePrefix("v")

if (!pluginVersion.isNullOrBlank()) {
  version = pluginVersion
}

// if aws creds & git tag is set, setup publishing
val awsKey = System.getenv()["AWS_ACCESS_KEY_ID"]
val awsSecret = System.getenv()["AWS_SECRET_ACCESS_KEY"]

if (listOf(tag, awsKey, awsSecret).none { it.isNullOrBlank() }) {
  publishing {
    repositories {
      maven("s3://***REMOVED***/releases/") {
        name = "s3"
        credentials(AwsCredentials::class.java) {
          accessKey = awsKey
          secretKey = awsSecret
        }
      }
    }
  }
}

group = "wtf.emulator"
