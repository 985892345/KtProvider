import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  android {
    namespace = "com.ktprovider.sample.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
}
