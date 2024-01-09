plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.publisher) apply false
  alias(libs.plugins.ksp) apply false
  id("io.github.985892345.KtProvider") version "1.3.1" apply false // 测试时记得改这里的版本号
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