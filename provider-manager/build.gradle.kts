import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  id("io.github.985892345.MavenPublisher")
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
  wasmJs()
  js(IR) {
    browser()
    nodejs()
  }
  
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":provider-api"))
      }
    }
  }
}

publisher {
  description = "the service provider manager of KtProvider"
}