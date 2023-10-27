plugins {
  kotlin("jvm")
  id("com.gradle.plugin-publish") version "1.2.1" // https://plugins.gradle.org/docs/publish-plugin
  id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
}

version = properties["VERSION"].toString()
group = properties["GROUP"].toString()

buildConfig {
  packageName("com.g985892345.provider.plugin.gradle")
  buildConfigField("String", "VERSION", "\"${properties["VERSION"].toString()}\"")
  buildConfigField("String", "GROUP", "\"${properties["GROUP"].toString()}\"")
  buildConfigField("String", "PLUGIN_ID", "\"${properties["PLUGIN_ID"].toString()}\"")
}

gradlePlugin {
  website.set("https://github.com/985892345/KtProvider")
  vcsUrl.set("https://github.com/985892345/KtProvider")
  plugins {
    create("KtProvider") {
      id = "io.github.985892345.KtProvider"
      implementationClass = "com.g985892345.provider.plugin.gradle.KtProviderGradlePlugin"
      displayName = "服务提供插件"
      description = "本插件使用 Kotlin Compiler Plugin 进行 IR 插桩，实现服务提供"
      tags.set(listOf("kotlin Multiplatform", "Kotlin Compiler Plugin", "Service Provider"))
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

tasks.create("publishToMavenCentral") {
  group = "publishing"
  dependsOn(tasks.getByName("publishAllPublicationsToMavenCentralRepository"))
}




