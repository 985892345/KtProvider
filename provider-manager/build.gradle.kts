import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publisher)
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  macosX64()
  macosArm64()
  linuxX64()
  linuxArm64()
  mingwX64()
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    nodejs()
  }
  js(IR) {
    browser()
    nodejs()
  }
  
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.providerApi)
      }
    }
  }
}

publisher {
  description = "the service provider manager of KtProvider"
}