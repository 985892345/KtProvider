plugins {
  kotlin("multiplatform") version "1.9.10" apply false
  kotlin("jvm") version "1.9.10" apply false
  id("com.github.gmazzo.buildconfig") version "4.0.4" apply false
}

allprojects {
  repositories {
    mavenCentral()
    mavenLocal()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}