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
  masterDeveloper = DeveloperInformation(
    githubName = "985892345",
    email = "guo985892345@formail.com"
  )
  description = "the KSP of KtProvider"
}
