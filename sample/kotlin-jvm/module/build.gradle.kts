plugins {
  kotlin("jvm")
  id("com.google.devtools.ksp")
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(kotlin("reflect"))
  
  implementation(rootProject.project("sample:kotlin-jvm:api"))
  implementation(rootProject.project("sample:kotlin-jvm:impl"))
  implementation(rootProject.project("sample:kotlin-jvm:impl2"))
  
  implementation(ktProvider.manager)
  ksp(ktProvider.ksp)
}
