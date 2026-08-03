package com.g985892345.provider.plugin.gradle.extensions

import com.g985892345.provider.plugin.gradle.BuildConfig
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * 2023/6/18 12:36
 */
abstract class KtProviderExtensions(private val project: Project) {
  
  /**
   * Plugin version.
   */
  val version = BuildConfig.VERSION

  /**
   * Whether KtProvider dependencies should resolve to projects in the current source build.
   * Published coordinates remain the default for regular plugin consumers.
   */
  var useLocalDependencies = false
  
  /**
   * KSP dependency for the corresponding version.
   */
  val ksp: Any
    get() = dependency("provider-compile-ksp", ":provider-compile-ksp")
  
  /**
   * provider-api dependency for the corresponding version.
   * It is already included by default when importing the plugin, so there is no need to add it again.
   */
  val api: Any
    get() = apiDependency("provider-api")
  
  /**
   * provider-manager dependency for the corresponding version.
   */
  val manager: Any
    get() = dependency("provider-manager", ":provider-manager")

  /**
   * provider-testing dependency for the corresponding version.
   */
  val testing: Any
    get() = dependency("provider-testing", ":provider-testing")
  
  /**
   * dependent configuration
   */
  var configurations = Regex("(api)|(implementation)|(.+Api)|(.+Implementation)")
  
  /**
   * Whether to enable logs
   */
  fun setLogEnable(enable: Boolean) {
    project.extensions.configure(KspExtension::class.java) {
      it.arg("ktProviderLogEnable", enable.toString())
    }
  }
  
  /**
   * Set the maximum number of iterations that can be handled by the polling process.
   * Defaults to 5 times.
   */
  fun setProcessMaxCount(processMaxCount: Int) {
    project.extensions.configure(KspExtension::class.java) {
      it.arg("ktProviderProcessMaxCount", processMaxCount.toString())
    }
  }

  internal fun apiDependency(artifactId: String): Any {
    return dependency(artifactId, ":provider-api")
  }

  private fun dependency(artifactId: String, localProjectPath: String): Any {
    if (!useLocalDependencies) {
      return "${BuildConfig.GROUP}:$artifactId:$version"
    }
    return project.rootProject.findProject(localProjectPath)
      ?: error("Local KtProvider dependency project not found: $localProjectPath")
  }
  
  companion object {
    
    fun getClassPackage(rootProject: Project, projectPath: String): String {
      val root = rootProject.name.lowercase().replace(Regex("[^0-9a-zA-Z.]"), "")
      val prefix = "com.g985892345.provider.$root"
      val pathPackageName = projectPath.replace(":", ".")
      return prefix + pathPackageName.lowercase().replace(Regex("[^0-9a-zA-Z.]"), "")
    }
    
    fun getClassNameSuffix(projectName: String): String {
      return projectName
        .split(Regex("[^0-9a-zA-Z]"))
        .joinToString("") { it.capitalized() }
    }
    
    fun getKtProviderInitializerClass(
      rootProject: Project,
      projectPath: String,
      projectName: String
    ): String {
      val path = getClassPackage(rootProject, projectPath)
      val name = getClassNameSuffix(projectName)
      return "$path.${name}KtProviderInitializer"
    }
    
    fun getKtProviderRouterClass(
      rootProject: Project,
      projectPath: String,
      projectName: String
    ): String {
      val path = getClassPackage(rootProject, projectPath)
      val name = getClassNameSuffix(projectName)
      return "$path.${name}KtProviderRouter"
    }
    
    private fun String.capitalized(): String {
      return replaceFirstChar {
        it.uppercaseChar()
      }
    }
  }
}
