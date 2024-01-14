import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  androidTarget {
    compilations.all {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
}

android {
  namespace = "com.ktprovider.sample.api"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
}
