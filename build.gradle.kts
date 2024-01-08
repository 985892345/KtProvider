plugins {
  kotlin("multiplatform") version "1.9.21" apply false
  kotlin("jvm") version "1.9.21" apply false
  id("com.github.gmazzo.buildconfig") version "4.0.4" apply false
  id("io.github.985892345.KtProvider") version "1.3.0" apply false // 测试时记得改这里的版本号
  id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false
  id("io.github.985892345.MavenPublisher") version "1.1.1" apply false
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