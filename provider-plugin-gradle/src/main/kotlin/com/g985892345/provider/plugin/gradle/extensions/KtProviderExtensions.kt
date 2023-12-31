package com.g985892345.provider.plugin.gradle.extensions

import com.g985892345.provider.plugin.gradle.BuildConfig
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
   * KSP dependency for the corresponding version.
   */
  val ksp = "${BuildConfig.GROUP}:provider-compile-ksp:${version}"
  
  /**
   * provider-api dependency for the corresponding version.
   * It is already included by default when importing the plugin, so there is no need to add it again.
   */
  val api = "${BuildConfig.GROUP}:provider-api:${version}"
  
  /**
   * provider-manager dependency for the corresponding version.
   */
  val manager = "${BuildConfig.GROUP}:provider-manager:${version}"
  
  companion object {
    
    fun getPackageName(project: Project): String {
      val prefix = "com.g985892345.provider."
      var packageName = project.name
      var p = project
      while (p.parent != null) {
        p = p.parent!!
        val projectNamePackage = p.name
        packageName = "${projectNamePackage}.$packageName"
      }
      packageName = prefix + packageName.lowercase().replace(Regex("[^0-9a-zA-Z.]"), "")
      return packageName
    }
    
    fun getClassNameSuffix(project: Project): String {
      return project.name
        .split(Regex("[^0-9a-zA-Z]"))
        .joinToString("") { it.capitalized() }
    }
    
    /**
     * Obtain the qualified name of the automatically generated KtProviderInitializer implementation class.
     */
    fun getInitializerClass(project: Project): String {
      return "${getPackageName(project)}." + getClassNameSuffix(project) + "KtProviderInitializer"
    }
    
    private fun String.capitalized(): String {
      return replaceFirstChar {
        it.uppercaseChar()
      }
    }
  }
}