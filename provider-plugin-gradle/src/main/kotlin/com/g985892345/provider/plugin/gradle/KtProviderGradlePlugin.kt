package com.g985892345.provider.plugin.gradle

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import com.g985892345.provider.plugin.gradle.generator.KtProviderInitializerGenerator
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
    KtProviderInitializerGenerator(target).config()
    // 添加对 IKtProviderInitializer 的依赖
    target.dependencies.add(
      "implementation",
      "io.github.985892345:provider-init:${BuildConfig.VERSION}"
    )
    // 添加对 IKtProviderInitializer 的依赖
    target.dependencies.add(
      "implementation",
      "io.github.985892345:provider-annotation:${BuildConfig.VERSION}"
    )
  }
  
  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    val ktProviderExtension = project.extensions.getByType(KtProviderExtensions::class.java)
    val isCheckImpl = ktProviderExtension.isCheckImpl.toString()
    return project.provider {
      mutableListOf<SubpluginOption>().apply {
        add(SubpluginOption("isCheckImpl", isCheckImpl))
      }
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