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
  wasmJs {
    browser()
    nodejs()
  }
  js(IR) {
    browser()
    nodejs()
  }
}

publisher {
  description = "the api of KtProvider"
}