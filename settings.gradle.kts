pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    mavenLocal()
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}
rootProject.name = "KtProvider"
include("provider-plugin-gradle")
include("provider-plugin-kcp")
include("provider-manager")
include("provider-annotation")
include("provider-init")
include("sample")
include("sample:kotlin-jvm")
include("sample:kotlin-jvm:api")
include("sample:kotlin-jvm:impl")
include("sample:kotlin-jvm:module")
