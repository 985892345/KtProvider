import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.ksp)
  id("io.github.985892345.KtProvider")
}

ktProvider {
  setLogEnable(true)
}

kotlin {
  jvm()
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ModuleApp"
      isStatic = true
    }
  }
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    moduleName = "moduleApp"
    browser {
      commonWebpackConfig {
        outputFileName = "moduleApp.js"
      }
    }
    binaries.executable()
  }
  
  sourceSets {
    commonMain.dependencies {
      implementation(projects.sample.kotlinMultiplatform.api)
      implementation(projects.sample.kotlinMultiplatform.impl)
      implementation(projects.providerApi)
      implementation(projects.providerManager)
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", projects.providerCompileKsp)
  add("kspAndroid", projects.providerCompileKsp)
  add("kspJvm", projects.providerCompileKsp)
  add("kspIosX64", projects.providerCompileKsp)
  add("kspIosArm64", projects.providerCompileKsp)
  add("kspIosSimulatorArm64", projects.providerCompileKsp)
  add("kspWasmJs", projects.providerCompileKsp)
}

android {
  namespace = "com.ktprovider.sample.module"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    applicationId = "com.ktprovider.sample"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}


// 用于设置 iOS 项目的 project.pbxproj
// 把模版中的 iosApp 放到模块目录下，然后运行该 task 进行修改，最后新增 iOS 配置项就可以跑起来了
// 如果启动模块依赖了新的其他模块，则需要再次运行该 task
tasks.register("setIOSProjectPbxproj") {
  group = "ios"
  val file = projectDir.resolve("iosApp")
    .resolve("iosApp.xcodeproj")
    .resolve("project.pbxproj")
  val dependProjects = project.configurations
    .getByName("commonMainImplementation")
    .dependencies
    .asSequence()
    .filterIsInstance<ProjectDependency>()
    .map { it.dependencyProject }
    .toList()
  inputs.property("dependProjects", dependProjects.map { it.path })
  outputs.file(file)
  doFirst {
    val rootProjectPath = "\$SRCROOT" + project.path.split(":").joinToString("") { "/.." }
    val lines = file.readLines().toMutableList()
    val iterator = lines.listIterator()
    while (iterator.hasNext()) {
      val line = iterator.next()
      if (line.contains("shellScript = ")) {
        if (line.contains("\$SRCROOT")) {
          iterator.set(
            line.substringBeforeLast("\$SRCROOT") +
                "${rootProjectPath}\\\"\\n./gradlew ${project.path}:embedAndSignAppleFrameworkForXcode\\n\";"
          )
        }
      }
      if (line.contains("FRAMEWORK_SEARCH_PATHS")) {
        while (iterator.hasNext() && !iterator.next().contains(";")) {
          iterator.remove()
        }
        iterator.previous()
        val space = line.substringBefore("FRAMEWORK_SEARCH_PATHS") + "    "
        (dependProjects + project).map {
          space + "\"" + rootProjectPath + it.path.replace(":", "/") + "/build/xcode-frameworks/\$(CONFIGURATION)/\$(SDK_NAME)\","
        }.forEach {
          iterator.add(it)
        }
      }
      if (line.contains("OTHER_LDFLAGS")) {
        iterator.next()
        val space = iterator.next().substringBefore("\"")
        iterator.next()
        iterator.remove()
        iterator.add("${space}ModuleApp,")
      }
    }
    file.writeText(lines.joinToString("\n"))
  }
}