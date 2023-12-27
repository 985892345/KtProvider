plugins {
  kotlin("jvm")
  id("com.github.gmazzo.buildconfig")
  id("com.gradle.plugin-publish") version "1.2.1" // https://plugins.gradle.org/docs/publish-plugin
  id("io.github.985892345.MavenPublisher")
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
  compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.20-1.0.14")
}

publisher {
  description = "the gradle plugin of KtProvider"
  createGradlePlugin(
    name = "KtProvider",
    id = "io.github.985892345.KtProvider",
    implementationClass = "com.g985892345.provider.plugin.gradle.KtProviderGradlePlugin",
    displayName = "服务提供插件",
    tags = listOf("kotlin Multiplatform", "KSP", "Service Provider")
  )
}

buildConfig {
  packageName("com.g985892345.provider.plugin.gradle")
  buildConfigField("String", "VERSION", "\"${properties["VERSION"].toString()}\"")
  buildConfigField("String", "GROUP", "\"${publisher.groupId}\"")
}



