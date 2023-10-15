package com.g985892345.provider.plugin.gradle.generator

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import org.gradle.api.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized

/**
 * .
 *
 * @author 985892345
 * 2023/10/15 11:11
 */
class KtProviderInitializerGenerator(
  val project: Project
) {
  
  private val ktProviderSource = project.buildDir.resolve("generated")
    .resolve("source")
    .resolve("ktProvider")
    .resolve(SourceSet.MAIN_SOURCE_SET_NAME)
  
  private val taskName = "generate${project.name.capitalized()}KtProviderInitializerImpl"
  
  fun config() {
    project.afterEvaluate { project ->
      val ktProviderExtension = project.extensions.getByType(KtProviderExtensions::class.java)
      if (ktProviderExtension.isAutoCreateKtProviderInitializer == true) {
        configCreateKtProvider(ktProviderExtension)
      }
    }
  }
  
  // 生成 IKtProviderInitializer 的实现类并加入编译环境
  private fun configCreateKtProvider(ktProviderExtension: KtProviderExtensions) {
    val initializerClassName = mutableListOf<Pair<String, String>>()
    // 解析所有依赖的模块
    listOf(
      "implementation",
      "api"
    ).map { project.configurations.getByName(it) }.forEach { config ->
      config.dependencies.forEach { dependency ->
        if (dependency is ProjectDependency) {
          if (dependency.dependencyProject.plugins.hasPlugin("io.github.985892345.KtProvider")) {
            val extension = dependency.dependencyProject.extensions.getByType(KtProviderExtensions::class.java)
            if (extension.isAutoCreateKtProviderInitializer != null) {
              initializerClassName.add(
                extension.initializerClassPackage to extension.initializerClassName
              )
            }
          }
        }
      }
    }
    // 生成 IKtProviderInitializer 的实现类
    val taskProvider = project.tasks.register(taskName) { task ->
      task.group = "ktProvider"
      task.inputs.property("initializerClassCanonicalName", initializerClassName)
      task.outputs.dir(ktProviderSource)
      task.doLast {
        val text = getKtProviderInitializerTemplate(ktProviderExtension, initializerClassName)
        var file = ktProviderSource
        ktProviderExtension.initializerClassPackage.split(".").forEach {
          file = file.resolve(it)
        }
        file.mkdirs()
        file = file.resolve("${ktProviderExtension.initializerClassName}.kt")
        file.createNewFile()
        file.writeText(
          "// 自动生成，task 为 ${task.name} \n" +
            text
        )
      }
    }
    // 加入编译环境
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.android",
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.js",
      "kotlin2js",
      "org.jetbrains.kotlin.multiplatform"
    ) {
      @Suppress("UNCHECKED_CAST")
      val sourceSet = project.extensions.getByName("kotlin").run {
        javaClass.getMethod("getSourceSets")
          .invoke(this) as NamedDomainObjectContainer<Named>
      }
      sourceSet.configureEach {
        (it.javaClass.getMethod("getKotlin")
          .invoke(it) as SourceDirectorySet)
          .srcDir(taskProvider)
      }
    }
  }
  
  private fun PluginContainer.withAnyId(vararg ids: String, action: Action<in Plugin<*>>) {
    ids.forEach { withId(it, action) }
  }
}

private fun getKtProviderInitializerTemplate(
  ktProviderExtension: KtProviderExtensions,
  initializerClassName: List<Pair<String, String>>
): String = """
  package ${ktProviderExtension.initializerClassPackage}
  
  import com.g985892345.provider.init.IKtProviderInitializer
  ${initializerClassName.joinToString("\n  ") { "import ${it.first}.${it.second}" }}
  
  object ${ktProviderExtension.initializerClassName} : IKtProviderInitializer {
    override fun initKtProvider() {
      ${initializerClassName.joinToString("\n      ") { "${it.second}.initKtProvider()" }}
    }
  }
""".trimIndent()