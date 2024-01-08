pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "KtProvider"
include("provider-plugin-gradle")
include("provider-compile-ksp")
include("provider-manager")
include("provider-api")
include("sample")
include("sample:kotlin-jvm")
include("sample:kotlin-jvm:api")
include("sample:kotlin-jvm:impl")
include("sample:kotlin-jvm:impl2")
include("sample:kotlin-jvm:module")
