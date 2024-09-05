import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import java.net.URI

class JavaConvention : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      target.version = providers.gradleProperty("VERSION_NAME").orNull ?: target.version

      with(pluginManager) {
        apply("java-library")
        apply("maven-publish")
        apply("com.vanniktech.maven.publish")
      }

      with(extensions.getByType(JavaPluginExtension::class.java)) {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        toolchain {
          languageVersion.set(JavaLanguageVersion.of(11))
          vendor.set(JvmVendorSpec.AZUL)
        }
      }

      // in-tree and mavenlocal publishing
      with(extensions.getByType(PublishingExtension::class.java)) {
        repositories {
          mavenLocal()
          maven {
            name = "intree"
            url = rootProject.layout.buildDirectory.dir("maven-repo").map { it.asFile.toURI() }.get()
          }
        }
      }

      // if aws creds & git tag is set, setup publishing to s3 as well
      val awsKey = System.getenv()["AWS_ACCESS_KEY_ID"]
      val awsSecret = System.getenv()["AWS_SECRET_ACCESS_KEY"]
      val awsBucket = System.getenv()["AWS_S3_BUCKET"]

      if (listOf(awsKey, awsSecret, awsBucket).none { it.isNullOrBlank() }) {
        with(extensions.getByType(PublishingExtension::class.java)) {
          repositories {
            val dir = if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
            maven {
              url = URI.create("s3://$awsBucket/$dir/")
              name = "s3"
              credentials(AwsCredentials::class.java) {
                accessKey = awsKey
                secretKey = awsSecret
              }
            }
          }
        }
      }
    }
  }
}
