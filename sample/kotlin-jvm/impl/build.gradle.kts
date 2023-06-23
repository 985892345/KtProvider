plugins {
  kotlin("jvm")
}

group = "org.example"
version = "unspecified"

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  
  implementation(rootProject.project("sample:kotlin-jvm:api"))
  
  val version = properties["VERSION"].toString()
  implementation("io.github.985892345:provider-annotation-jvm:$version")
}

tasks.test {
  useJUnitPlatform()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}