package com.g985892345.provider.plugin.gradle.generator

import com.g985892345.provider.plugin.gradle.BuildConfig
import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import com.google.devtools.ksp.gradle.KspExtension
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
    project.dependencies.add(
      "ksp",
      "io.github.985892345:provider-compile-ksp:${BuildConfig.VERSION}"
    )
  }
  
  // 生成 KtProviderInitializer 的实现类并加入编译环境
  private fun configCreateKtProviderTask(): TaskProvider<Task> {
    val ktProviderRouterPackageName = KtProviderExtensions.getPackageName(project)
    val ktProviderRouterClassName = "${KtProviderExtensions.getClassNameSuffix(project)}KtProviderRouter"
    project.extensions.configure(KspExtension::class.java) {
      it.arg("ktProviderRouterPackageName", ktProviderRouterPackageName)
      it.arg("ktProviderRouterClassName", ktProviderRouterClassName)
    }
    // 生成 KtProviderInitializer 的实现类
    return project.tasks.register(taskName) { task ->
      task.group = "ktProvider"
      val dependModuleProjects = getDependModulePaths()
      task.inputs.property("dependModulePaths", dependModuleProjects.map { it.path })
      task.outputs.dir(ktProviderSource)
      task.doLast {
        val selfInitializerClass = KtProviderExtensions.getInitializerClass(project)
        val text = getKtProviderInitializerTemplate(
          selfInitializerClass,
          "$ktProviderRouterPackageName.$ktProviderRouterClassName",
          dependModuleProjects.filter { dependProject ->
            dependProject.extensions.findByType(KtProviderExtensions::class.java) != null
          }.map { dependProject ->
            KtProviderExtensions.getInitializerClass(dependProject)
          },
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
          fileText
    )
  }
}

private fun getKtProviderInitializerTemplate(
  selfInitializerClass: String,
  ktProviderRouterClass: String,
  invokeInitializerClass: List<String>,
): String = """
  package ${selfInitializerClass.substringBeforeLast(".")}
  
  import com.g985892345.provider.init.KtProviderInitializer
  import com.g985892345.provider.init.KtProviderRouter
  
  object ${selfInitializerClass.substringAfterLast(".")} : KtProviderInitializer() {
  
    override val router: KtProviderRouter = $ktProviderRouterClass
    
    override val otherModuleKtProvider: List<KtProviderInitializer> = listOf(
      ${invokeInitializerClass.joinToString(",\n      ")}
    )
  }
""".trimIndent()
