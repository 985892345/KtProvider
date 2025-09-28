import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

ktProvider {
  setLogEnable(true)
}

kotlin {
  jvm()
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ModuleApp"
      isStatic = true
    }
  }
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    outputModuleName.set("moduleApp")
    browser {
      commonWebpackConfig {
        outputFileName = "moduleApp.js"
      }
    }
    binaries.executable()
  }
  
  sourceSets {
    commonMain.dependencies {
      implementation(projects.sample.kotlinMultiplatform.api)
      implementation(projects.sample.kotlinMultiplatform.impl)
      implementation(projects.providerApi)
      implementation(projects.providerManager)
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
  namespace = "com.ktprovider.sample.module"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    applicationId = "com.ktprovider.sample"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}