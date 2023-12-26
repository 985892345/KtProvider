import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  id("publish-maven-central")
}

group = properties["GROUP"].toString()
version = properties["VERSION"].toString()

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

publish {
  artifactId = "provider-manager"
}