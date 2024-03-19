plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publisher)
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  // macosX64()
  // macosArm64()
  // linuxX64()
  // linuxArm64()
  // mingwX64()
  // wasmJs {
  //   browser()
  //   nodejs()
  // }
  // js(IR) {
  //   browser()
  //   nodejs()
  // }
}

publisher {
  description = "the api of KtProvider"
}