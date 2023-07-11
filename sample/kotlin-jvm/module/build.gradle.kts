plugins {
  kotlin("jvm")
  id("io.github.985892345.KtProvider") version "1.0.1-alpha14-SNAPSHOT" // 测试时记得改这里的版本号
}

group = "org.example"
version = "unspecified"

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  
  implementation(kotlin("reflect"))
  
  implementation(rootProject.project("sample:kotlin-jvm:api"))
  implementation(rootProject.project("sample:kotlin-jvm:impl"))
  
  val version = properties["VERSION"].toString()
  implementation("io.github.985892345:provider-init-jvm:$version")
  implementation("io.github.985892345:provider-manager-jvm:$version")
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

ktProvider {
  packageName {
    include("com.g985892345.test")
  }
}