plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.publisher) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.jetbrains.compose) apply false
  id("io.github.985892345.KtProvider") version "1.3.2-alpha02-SNAPSHOT" apply false // 测试时记得改这里的版本号
}

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}