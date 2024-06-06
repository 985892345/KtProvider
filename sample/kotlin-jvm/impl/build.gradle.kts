plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

ktProvider.setLogEnable(true)

dependencies {
  implementation(projects.sample.kotlinJvm.api)
  implementation(projects.providerApi)
  ksp(projects.providerCompileKsp)
}
