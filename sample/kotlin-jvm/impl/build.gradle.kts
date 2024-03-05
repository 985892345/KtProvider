plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

ktProvider.setLogEnable(true)
ktProvider.setProcessTimes(2)

dependencies {
  implementation(projects.sample.kotlinJvm.api)
  ksp(ktProvider.ksp)
}
