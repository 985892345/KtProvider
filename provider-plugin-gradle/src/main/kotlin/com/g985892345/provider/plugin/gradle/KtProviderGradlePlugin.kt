package com.g985892345.provider.plugin.gradle

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 22:53
 */
class KtProviderGradlePlugin : KotlinCompilerPluginSupportPlugin {
  
  override fun apply(target: Project) {
    super.apply(target)
    target.extensions.create("ktProvider", KtProviderExtensions::class.java, target)
  }
  
  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    val ktProviderExtensions = project.extensions.getByType(KtProviderExtensions::class.java)
    val nameMatcher = ktProviderExtensions.packageNameManager.nameMatcher
    val name = if (nameMatcher.isEmpty()) "" else nameMatcher.joinToString("&")
    return project.provider {
      listOf(
        SubpluginOption("package", name)
      )
    }
  }
  
  override fun getCompilerPluginId(): String {
    return BuildConfig.PLUGIN_ID
  }
  
  override fun getPluginArtifact(): SubpluginArtifact {
    return SubpluginArtifact(
      groupId = BuildConfig.GROUP,
      artifactId = "KtProvider-kcp",
      version = BuildConfig.VERSION,
    )
  }
  
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    return true
  }
}