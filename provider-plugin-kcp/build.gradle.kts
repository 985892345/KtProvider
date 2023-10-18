plugins {
  kotlin("jvm")
  kotlin("kapt")
  kotlin("plugin.serialization")
  id("publish-maven-central")
  id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}

tasks.test {
  useJUnitPlatform()
}

version = properties["VERSION"].toString()
group = properties["GROUP"].toString()

buildConfig {
  packageName("com.g985892345.provider.plugin.kcp")
  buildConfigField("String", "VERSION", "\"${properties["VERSION"].toString()}\"")
  buildConfigField("String", "GROUP", "\"${properties["GROUP"].toString()}\"")
  buildConfigField("String", "PLUGIN_ID", "\"${properties["PLUGIN_ID"].toString()}\"")
}

publish {
  artifactId = "KtProvider-kcp"
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
  kapt("com.google.auto.service:auto-service:1.0.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
  
  testImplementation(kotlin("test"))
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0") // https://github.com/tschuchortdev/kotlin-compile-testing
}