plugins {
  kotlin("jvm")
  id("com.gradle.plugin-publish") version "1.2.1" // https://plugins.gradle.org/docs/publish-plugin
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
  compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.20-1.0.14")
}

version = properties["VERSION"].toString()
group = properties["GROUP"].toString()

gradlePlugin {
  website.set("https://github.com/985892345/KtProvider")
  vcsUrl.set("https://github.com/985892345/KtProvider")
  plugins {
    create("KtProvider") {
      id = "io.github.985892345.KtProvider"
      implementationClass = "com.g985892345.provider.plugin.gradle.KtProviderGradlePlugin"
      displayName = "服务提供插件"
      description = "实现 KSP 实现的服务提供框架"
      tags.set(listOf("kotlin Multiplatform", "KSP", "Service Provider"))
    }
  }
}

publishing {
  publications {
    repositories {
      maven {
        // https://s01.oss.sonatype.org/
        name = "mavenCentral"
        val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        val isSnapshot = version.toString().endsWith("SNAPSHOT")
        setUrl(if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl)
        credentials {
          username = project.properties["mavenCentralUsername"].toString()
          password = project.properties["mavenCentralPassword"].toString()
        }
      }
    }
  }
}

// 补充 publishToMavenCentral task 用于一键发版
tasks.create("publishToMavenCentral") {
  group = "publishing"
  dependsOn(tasks.getByName("publishAllPublicationsToMavenCentralRepository"))
}




