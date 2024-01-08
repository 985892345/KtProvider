plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(kotlin("reflect"))
  
  implementation(projects.sample.kotlinJvm.api)
  implementation(projects.sample.kotlinJvm.impl)
  implementation(projects.sample.kotlinJvm.impl2)
  
  implementation(ktProvider.manager)
  ksp(ktProvider.ksp)
}
