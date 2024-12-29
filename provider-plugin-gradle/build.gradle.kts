plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.publisher)
  `java-gradle-plugin`
}

dependencies {
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly(libs.ksp.gradlePlugin)
}

publisher {
  masterDeveloper = DeveloperInformation(
    githubName = "985892345",
    email = "guo985892345@formail.com"
  )
  description = "the gradle plugin of KtProvider"
  version = gradle.parent!!.rootProject.properties["VERSION"].toString()
  createGradlePlugin(
    name = "KtProvider",
    id = "io.github.985892345.KtProvider",
    implementationClass = "com.g985892345.provider.plugin.gradle.KtProviderGradlePlugin",
    displayName = "服务提供插件",
    tags = listOf("kotlin Multiplatform", "KSP", "Service Provider")
  )
  licenseFile = { project.gradle.parent!!.rootProject.rootDir.resolve("LICENSE") }
}

buildConfig {
  packageName("com.g985892345.provider.plugin.gradle")
  buildConfigField("String", "VERSION", "\"${gradle.parent?.rootProject!!.properties["VERSION"].toString()}\"")
  buildConfigField("String", "GROUP", "\"${publisher.groupId}\"")
}



