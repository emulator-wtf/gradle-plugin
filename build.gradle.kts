import com.vanniktech.maven.publish.SonatypeHost

plugins {
  `java-gradle-plugin`
  `maven-publish`
  id("com.vanniktech.maven.publish")
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

mavenPublish {
  sonatypeHost = SonatypeHost.S01
}

// if aws creds & git tag is set, setup publishing
val awsKey = System.getenv()["AWS_ACCESS_KEY_ID"]
val awsSecret = System.getenv()["AWS_SECRET_ACCESS_KEY"]
val awsBucket = System.getenv()["AWS_S3_BUCKET"]

if (listOf(awsKey, awsSecret, awsBucket).none { it.isNullOrBlank() }) {
  publishing {
    repositories {
      val dir = if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
      maven("s3://$awsBucket/$dir/") {
        name = "s3"
        credentials(AwsCredentials::class.java) {
          accessKey = awsKey
          secretKey = awsSecret
        }
      }
    }
  }
}
