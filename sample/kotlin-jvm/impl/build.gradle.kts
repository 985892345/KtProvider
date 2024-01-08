plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(projects.sample.kotlinJvm.api)
  ksp(ktProvider.ksp)
}
