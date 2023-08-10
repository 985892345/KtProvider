plugins {
  kotlin("multiplatform")
  id("publish-maven-central")
}

group = properties["GROUP"].toString()
version = properties["VERSION"].toString()

kotlin {
  jvm {
    jvmToolchain(11)
    withJava()
    testRuns.named("test") {
      executionTask.configure {
        useJUnitPlatform()
      }
    }
  }
  js {
    browser {
      commonWebpackConfig(Action {
        cssSupport {
          enabled.set(true)
        }
      })
    }
  }
  val hostOs = System.getProperty("os.name")
  val isArm64 = System.getProperty("os.arch") == "aarch64"
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
    hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
    hostOs == "Linux" && isArm64 -> linuxArm64("native")
    hostOs == "Linux" && !isArm64 -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }
  
  sourceSets {
    val commonMain by getting {
      dependencies {
        // Kotlin/Native 不支持 compileOnly，所以使用 implementation
        implementation(project(":provider-init"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

publish {
  artifactId = "provider-manager"
}