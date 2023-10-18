plugins {
  kotlin("jvm")
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(kotlin("reflect"))
  
  implementation(rootProject.project("sample:kotlin-jvm:api"))
  implementation(rootProject.project("sample:kotlin-jvm:impl"))
  implementation(rootProject.project("sample:kotlin-jvm:impl2"))
  
  val version = properties["VERSION"].toString()
  implementation("io.github.985892345:provider-manager-jvm:$version")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}