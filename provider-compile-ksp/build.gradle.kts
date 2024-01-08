plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("io.github.985892345.MavenPublisher")
}

dependencies {
  implementation(project(":provider-api"))
  
  // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
  compileOnly("com.google.auto.service:auto-service:1.1.1")
  kapt("com.google.auto.service:auto-service:1.1.1")
  
  compileOnly("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.16")
  
  // https://square.github.io/kotlinpoet/
  implementation("com.squareup:kotlinpoet:1.15.1")
  implementation("com.squareup:kotlinpoet-ksp:1.15.1")
}

publisher {
  description = "the KSP of KtProvider"
}
