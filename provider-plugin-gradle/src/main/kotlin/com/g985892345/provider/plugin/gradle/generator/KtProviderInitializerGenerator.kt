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
  
  companion object {
    /**
     * 用于生成 KtProviderInitializer 实现类的 task 任务
     */
    fun getTaskName(project: Project): String {
      val projectName = project.name.split(Regex("[^0-9a-zA-Z]"))
        .joinToString("") { it.capitalized() }
      return "generate${projectName}KtProviderInitializerImpl"
    }
  }
  
  private val ktProviderSource = project.layout.buildDirectory.dir(
    "generated/ktProvider/${SourceSet.MAIN_SOURCE_SET_NAME}"
  )
  
  private val taskName = getTaskName(project)
  
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
      if (ktProviderExtension.enableKcp) {
        // 如果需要 ir 插桩，则需要解决 compileKotlin 任务的缓存问题
        // 以下写法将该 task 的缓存跟全部源集内的代码相关联，
        // 如果模块内代码有改动，则重新生成 KtProviderInitializer 实现类
        // 如果不重新生成，则会导致 compileKotlin 不会重新构建该类，导致 ir 插桩失败
        project.extensions
          .getByType(KotlinSourceSetContainer::class.java)
          .sourceSets
          .filter { ktProviderExtension.sourceSet.isEmpty() || it.name in ktProviderExtension.sourceSet }
          .forEach { sourceSet ->
            sourceSet.kotlin
              .srcDirs
              .filter { it.exists() }
              .forEach {
                task.inputs.dir(it)
              }
          }
      }
      val dependModuleProjects = getDependModulePaths()
      val delegate = ktProviderExtension.delegateClass
      task.inputs.property("dependModulePaths", dependModuleProjects.map { it.path })
      task.inputs.property("delegate", delegate ?: "")
      task.outputs.dir(ktProviderSource)
      task.doLast {
        val selfInitializerClass = KtProviderExtensions.getInitializerClass(project)
        val text = getKtProviderInitializerTemplate(
          selfInitializerClass,
          dependModuleProjects.filter { dependProject ->
            dependProject.extensions.findByType(KtProviderExtensions::class.java) != null
          }.map { dependProject ->
            KtProviderExtensions.getInitializerClass(dependProject)
          },
          delegate
        )
        outputFile(selfInitializerClass, it.name, text)
      }
    }
  }
  
  private fun configSourceSetSrcDir(taskProvider: TaskProvider<Task>) {
    project.extensions
      .getByType(KotlinSourceSetContainer::class.java)
      .sourceSets
      .configureEach {
        it.kotlin.srcDirs(taskProvider)
      }
  }
  
  // 获取依赖的所有模块 path
  private fun getDependModulePaths(): List<Project> {
    val dependProjects = mutableListOf<Project>()
    listOf(
      "api",
      "implementation",
    ).map { project.configurations.getByName(it) }.forEach { config ->
      config.dependencies.forEach { dependency ->
        if (dependency is ProjectDependency) {
          val dependProject = dependency.dependencyProject
          dependProjects.add(dependProject)
        }
      }
    }
    return dependProjects
  }
  
  // 输出文件
  private fun outputFile(
    selfInitializerClass: String,
    taskName: String,
    fileText: String,
  ) {
    var dir = ktProviderSource.get().asFile
    dir.deleteRecursively()
    selfInitializerClass.substringBeforeLast(".").split(".").forEach {
      dir = dir.resolve(it)
    }
    dir.mkdirs()
    val ktProviderImplFile = dir.resolve("${selfInitializerClass.substringAfterLast(".")}.kt")
    ktProviderImplFile.createNewFile()
    ktProviderImplFile.writeText(
      "// 自动生成，task 为 $taskName \n" +
          "// 时间戳: ${System.currentTimeMillis()} (用于构造不同的源文件，解决 compileKotlin 缓存问题) \n" +
          fileText
    )
  }
}

private fun getKtProviderInitializerTemplate(
  selfInitializerClass: String,
  invokeInitializerClass: List<String>,
  delegateClass: String?,
): String = """
  package ${selfInitializerClass.substringBeforeLast(".")}
  
  import com.g985892345.provider.init.KtProviderInitializer
  import kotlin.reflect.KClass
  
  object ${selfInitializerClass.substringAfterLast(".")} : KtProviderInitializer() {
  
    override fun initKtProvider() {
      ${if (delegateClass != null) "$delegateClass.onSuperInitKtProviderBefore()" else ""}
      super.initKtProvider()
      ${if (delegateClass != null) "$delegateClass.onSuperInitKtProviderAfter()" else ""}
    }
  
    override fun initAddAllProvider() {
      super.initAddAllProvider()
      ${if (delegateClass != null) "$delegateClass.onSelfAllProviderFinish()" else ""}
      ${invokeInitializerClass.joinToString("\n      ") { "${it}.tryInitKtProvider()" }}
    }
    
    override fun <T : Any> addImplProvider(clazz: KClass<T>, name: String, init: () -> T) {
      ${if (delegateClass != null) "if ($delegateClass.onAddImplProvider(clazz, name, init)) super.addImplProvider(clazz, name, init)"
        else "super.addImplProvider(clazz, name, init)"}
    }
    
    override fun <T : Any> addKClassProvider(clazz: KClass<T>, name: String, init: () -> KClass<out T>) {
      ${if (delegateClass != null) "if ($delegateClass.onAddKClassProvider(clazz, name, init)) super.addKClassProvider(clazz, name, init)"
        else "super.addKClassProvider(clazz, name, init)"}
    }
  }
""".trimIndent()
