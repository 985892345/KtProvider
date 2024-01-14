import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

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
  
  sourceSets {
    commonMain.dependencies {
      implementation(projects.sample.kotlinMultiplatform.api)
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", ktProvider.ksp)
  add("kspAndroid", ktProvider.ksp)
  add("kspJvm", ktProvider.ksp)
  add("kspIosX64", ktProvider.ksp)
  add("kspIosArm64", ktProvider.ksp)
  add("kspIosSimulatorArm64", ktProvider.ksp)
  add("kspWasmJs", ktProvider.ksp)
}

android {
  namespace = "com.ktprovider.sample.impl"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
}