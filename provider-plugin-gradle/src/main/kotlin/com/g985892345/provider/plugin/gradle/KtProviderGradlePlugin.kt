package com.g985892345.provider.plugin.gradle

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import com.g985892345.provider.plugin.gradle.generator.KtProviderInitializerGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 22:53
 */
class KtProviderGradlePlugin : Plugin<Project> {
  
  override fun apply(target: Project) {
    target.extensions.create("ktProvider", KtProviderExtensions::class.java, target)
    KtProviderInitializerGenerator(target).config()
  }
}