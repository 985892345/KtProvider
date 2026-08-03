plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.ktprovider.sample.android"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.ktprovider.sample"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  implementation(projects.sample.kotlinMultiplatform.module)
  implementation(projects.sample.kotlinMultiplatform.api)
  implementation(projects.providerApi)
  implementation(projects.providerManager)
}
