plugins {
  kotlin("multiplatform") version "1.9.10" apply false
  kotlin("jvm") version "1.9.10" apply false
  id("com.github.gmazzo.buildconfig") version "4.0.4" apply false
  id("io.github.985892345.KtProvider") version "1.0.1-alpha68-SNAPSHOT" apply false // 测试时记得改这里的版本号
}

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}