import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
  
  sourceSets {
    commonMain.dependencies {
      implementation(projects.sample.kotlinMultiplatform.api)
      implementation(projects.providerApi)
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", projects.providerCompileKsp)
  add("kspAndroid", projects.providerCompileKsp)
  add("kspJvm", projects.providerCompileKsp)
  add("kspIosX64", projects.providerCompileKsp)
  add("kspIosArm64", projects.providerCompileKsp)
  add("kspIosSimulatorArm64", projects.providerCompileKsp)
  add("kspWasmJs", projects.providerCompileKsp)
}

android {
  namespace = "com.ktprovider.sample.impl"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
}