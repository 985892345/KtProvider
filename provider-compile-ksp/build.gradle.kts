plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.publisher)
}

dependencies {
  implementation(projects.providerApi)
  compileOnly(libs.ksp.api)
  // https://square.github.io/kotlinpoet/
  implementation(libs.kotlinpoet)
}

publisher {
  description = "the KSP of KtProvider"
}
