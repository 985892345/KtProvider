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
   * 插件版本
   */
  val version = BuildConfig.VERSION
  
  /**
   * 对应版本的 ksp 依赖
   */
  val ksp = "${BuildConfig.GROUP}:provider-compile-ksp:${version}"
  
  /**
   * 对应版本的 provider-api 依赖，引入插件时已默认包含，无需再次依赖
   */
  val api = "${BuildConfig.GROUP}:provider-api:${version}"
  
  /**
   * 对应版本的 provider-manager 依赖
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
     * 得到自动生成的 KtProviderInitializer 实现类全称
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