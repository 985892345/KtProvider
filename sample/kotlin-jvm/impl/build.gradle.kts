plugins {
  kotlin("jvm")
  id("com.google.devtools.ksp")
  id("io.github.985892345.KtProvider")
}

group = "org.example"
version = "unspecified"

dependencies {
  implementation(rootProject.project("sample:kotlin-jvm:api"))
  ksp(ktProvider.ksp)
}
