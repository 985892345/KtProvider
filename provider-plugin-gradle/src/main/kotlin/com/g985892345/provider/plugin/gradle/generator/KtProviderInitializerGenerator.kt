package com.g985892345.provider.plugin.gradle.generator

import com.g985892345.provider.plugin.gradle.BuildConfig
import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import org.gradle.api.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

/**
 * .
 *
 * @author 985892345
 * 2023/10/15 11:11
 */
class KtProviderInitializerGenerator(
  val project: Project
) {
  
  private val ktProviderSource = project.layout.buildDirectory.dir(
    "generated/source/ktProvider/${SourceSet.MAIN_SOURCE_SET_NAME}"
  )
  
  private val taskName = "generate${project.name.capitalized()}KtProviderInitializerImpl"
  
  fun config() {
    configDependencies()
    val taskProvider = configCreateKtProviderTask()
    configSourceSetSrcDir(taskProvider)
  }
  
  private fun configDependencies() {
    // 添加对 KtProviderInitializer 的依赖
    project.dependencies.add(
      "implementation",
      "io.github.985892345:provider-init:${BuildConfig.VERSION}"
    )
    // 添加对 Provider 注解的依赖
    project.dependencies.add(
      "implementation",
      "io.github.985892345:provider-annotation:${BuildConfig.VERSION}"
    )
  }
  
  // 生成 KtProviderInitializer 的实现类并加入编译环境
  private fun configCreateKtProviderTask(): TaskProvider<Task> {
    val ktProviderExtension = project.extensions.getByType(KtProviderExtensions::class.java)
    // 生成 KtProviderInitializer 的实现类
    return project.tasks.register(taskName) { task ->
      task.group = "ktProvider"
      val initializerClassName = getInitializerClassName()
      project.extensions.getByType(KotlinSourceSetContainer::class.java).sourceSets.forEach { sourceSet ->
        // 该任务的缓存跟整个模块相关联，如果模块内代码有改动，则重新生成 KtProviderInitializer 实现类
        // 如果不重新生成，则会导致 compileKotlin 不会重新构建该类，导致 ir 插桩失败
        sourceSet.kotlin
          .srcDirs
          .filter { it.exists() }
          .forEach {
            task.inputs.dir(it)
          }
      }
      task.inputs.property("initializerClassName", ktProviderExtension.initializerClassName)
      task.inputs.property("initializerClassPackage", ktProviderExtension.initializerClassPackage)
      task.inputs.property("mBeforeFunctions", ktProviderExtension.mBeforeFunctions)
      task.inputs.property("mAfterFunctions", ktProviderExtension.mAfterFunctions)
      task.outputs.dir(ktProviderSource)
      task.doLast {
        val text = getKtProviderInitializerTemplate(
          ktProviderExtension, initializerClassName,
          ktProviderExtension.mBeforeFunctions,
          ktProviderExtension.mAfterFunctions,
        )
        outputFile(ktProviderExtension, it.name, text)
      }
    }
  }
  
  private fun configSourceSetSrcDir(taskProvider: TaskProvider<Task>) {
    project.extensions
      .getByType(KotlinSourceSetContainer::class.java)
      .sourceSets
      .configureEach {
        it.kotlin.srcDir(taskProvider)
      }
  }
  
  // 获取依赖模块的包名和类名
  private fun getInitializerClassName(): List<Pair<String, String>> {
    val initializerClassName = mutableListOf<Pair<String, String>>()
    // 解析所有依赖的模块 KtProviderInitializer 实现类
    listOf(
      "implementation",
      "api"
    ).map { project.configurations.getByName(it) }.forEach { config ->
      config.dependencies.forEach { dependency ->
        if (dependency is ProjectDependency) {
          val dependProject = dependency.dependencyProject
          if (dependProject.plugins.hasPlugin("io.github.985892345.KtProvider")) {
            val extension = dependProject.extensions.getByType(KtProviderExtensions::class.java)
            if (!extension.isLinked) {
              extension.isLinked = true
              initializerClassName.add(
                extension.initializerClassPackage to extension.initializerClassName
              )
            }
          }
        }
      }
    }
    return initializerClassName
  }
  
  // 输出文件
  private fun outputFile(
    ktProviderExtension: KtProviderExtensions,
    taskName: String,
    fileText: String
  ) {
    var file = ktProviderSource.get().asFile
    file.deleteRecursively()
    println(
      ".(${Exception().stackTrace[0].run { "$fileName:$lineNumber" }}) -> " +
        "outputFile = $file"
    )
    ktProviderExtension.initializerClassPackage.split(".").forEach {
      file = file.resolve(it)
    }
    file.mkdirs()
    file = file.resolve("${ktProviderExtension.initializerClassName}.kt")
    file.createNewFile()
    file.writeText(
      "// 自动生成，task 为 $taskName \n" +
        "// 时间戳: ${System.currentTimeMillis()} (用于构造不同的源文件，解决 compileKotlin 缓存问题) \n" +
        fileText
    )
    // todo 后续采取更好的方法解决 compileKotlin 缓存问题
  }
}

private fun getKtProviderInitializerTemplate(
  ktProviderExtension: KtProviderExtensions,
  initializerClassName: List<Pair<String, String>>,
  beforeFunctions: List<KtProviderExtensions.Function>,
  afterFunctions: List<KtProviderExtensions.Function>,
): String = """
  package ${ktProviderExtension.initializerClassPackage}
  
  import com.g985892345.provider.init.KtProviderInitializer
  ${initializerClassName.joinToString("\n  ") { "import ${it.first}.${it.second}" }}
  ${beforeFunctions.joinToString("\n  ") { it.import }}
  ${afterFunctions.joinToString("\n  ") { it.import }}
  
  object ${ktProviderExtension.initializerClassName} : KtProviderInitializer() {
    override fun initKtProvider() {
      ${beforeFunctions.joinToString("\n      ") { it.invoke }}
      super.initKtProvider()
      ${initializerClassName.joinToString("\n      ") { "${it.second}.initKtProvider()" }}
      ${afterFunctions.joinToString("\n      ") { it.invoke }}
    }
  }
""".trimIndent()