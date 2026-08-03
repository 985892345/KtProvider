import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    commonMain.dependencies {
      api(projects.providerApi)
      implementation(projects.providerManager)
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}

publisher {
  masterDeveloper = DeveloperInformation(
    githubName = "985892345",
    email = "guo985892345@formail.com"
  )
  description = "test support for KtProvider"
}
