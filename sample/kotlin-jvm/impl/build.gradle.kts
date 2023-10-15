plugins {
  kotlin("jvm")
  id("io.github.985892345.KtProvider") version "1.0.1-alpha55-SNAPSHOT" // 测试时记得改这里的版本号
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(rootProject.project("sample:kotlin-jvm:api"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}