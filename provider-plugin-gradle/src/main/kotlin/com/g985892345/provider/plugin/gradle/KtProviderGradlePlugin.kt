package com.g985892345.provider.plugin.gradle

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.PluginContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 22:53
 */
class KtProviderGradlePlugin : Plugin<Project> {
  
  override fun apply(target: Project) {
    target.extensions.create("ktProvider", KtProviderExtensions::class.java, target)
    config(target)
  }
  
  private fun config(project: Project) {
    val ktProvider = project.extensions.getByType(KtProviderExtensions::class.java)
    configDependencies(project, ktProvider)
    transmitKsp(project, ktProvider)
  }
  
  /**
   * Injects the KtProvider API according to the target module type.
   *
   * AGP 9 provides built-in Kotlin support and no longer requires
   * `org.jetbrains.kotlin.android`, so both Android application and library plugins must be observed.
   */
  private fun configDependencies(project: Project, ktProvider: KtProviderExtensions) {
    fun apiDependency(artifactId: String) = project.providers.provider<Any> {
      ktProvider.apiDependency(artifactId)
    }
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      project.extensions
        .getByType(KotlinSourceSetContainer::class.java)
        .sourceSets
        .named("commonMain") {
          it.dependencies {
            implementation(apiDependency("provider-api"))
          }
        }
    }
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.android",
      "com.android.application",
      "com.android.library",
      "org.jetbrains.kotlin.jvm",
    ) {
      project.dependencies.add(
        "implementation",
        apiDependency("provider-api-jvm")
      )
    }
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.js",
      "kotlin2js",
    ) {
      project.dependencies.add(
        "implementation",
        apiDependency("provider-api-js")
      )
    }
  }
  
  private fun transmitKsp(project: Project, ktProvider: KtProviderExtensions) {
    val ktProviderRouterClass = KtProviderExtensions.getKtProviderRouterClass(
      rootProject = project.rootProject,
      projectPath = project.path,
      projectName = project.name
    )
    val ktProviderRouterPackageName = ktProviderRouterClass.substringBeforeLast(".")
    val ktProviderRouterClassName = ktProviderRouterClass.substringAfterLast(".")
    val selfInitializerClass = KtProviderExtensions.getKtProviderInitializerClass(
      rootProject = project.rootProject,
      projectPath = project.path,
      projectName = project.name
    )
    val initializerPackageName = selfInitializerClass.substringBeforeLast(".")
    val initializerClassName = selfInitializerClass.substringAfterLast(".")
    project.extensions.configure(KspExtension::class.java) {
      it.arg("ktProviderRouterPackageName", ktProviderRouterPackageName)
      it.arg("ktProviderRouterClassName", ktProviderRouterClassName)
      it.arg("ktProviderInitializerPackageName", initializerPackageName)
      it.arg("ktProviderInitializerClassName", initializerClassName)
    }
    project.afterEvaluate {
      project.afterEvaluate {
        project.extensions.configure(KspExtension::class.java) {
          it.arg(
            "ktProviderDependModuleProjects",
            getDependProjectKtProviderInitializerClass(project, ktProvider)
              .joinToString(" + ")
          )
        }
      }
    }
  }
  
  private fun PluginContainer.withAnyId(vararg ids: String, action: Action<in Plugin<*>>) {
    ids.forEach { withId(it, action) }
  }
  
  // Retrieve the paths of all dependent modules.
  private fun getDependProjectKtProviderInitializerClass(project: Project, ktProvider: KtProviderExtensions): List<String> {
    val dependProjects = mutableListOf<String>()
    project.configurations.configureEach { config ->
      if (config.name.matches(ktProvider.configurations) && !config.name.isTestConfiguration()) {
        config.dependencies.forEach { dependency ->
          if (dependency is ProjectDependency) {
            val classPackageName = KtProviderExtensions.getKtProviderInitializerClass(
              rootProject = project.rootProject,
              projectPath = dependency.path,
              projectName = dependency.name
            )
            dependProjects.add(classPackageName)
          }
        }
      }
    }
    return dependProjects
  }

  private fun String.isTestConfiguration(): Boolean {
    return startsWith("test", ignoreCase = true) || contains("Test")
  }
}
