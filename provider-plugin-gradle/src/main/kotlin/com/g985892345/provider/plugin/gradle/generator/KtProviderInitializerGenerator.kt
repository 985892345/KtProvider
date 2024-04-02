package com.g985892345.provider.plugin.gradle.generator

import com.g985892345.provider.plugin.gradle.extensions.KtProviderExtensions
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.PluginContainer
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
     * Task used for generating the implementation class of KtProviderInitializer.
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
    val ktProvider = project.extensions.getByType(KtProviderExtensions::class.java)
    configDependencies(ktProvider)
    val taskProvider = configCreateKtProviderTask(ktProvider)
    configSourceSetSrcDir(taskProvider)
  }
  
  private fun configDependencies(ktProvider: KtProviderExtensions) {
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      project.extensions
        .getByType(KotlinSourceSetContainer::class.java)
        .sourceSets
        .named("commonMain") {
          it.dependencies {
            implementation(ktProvider.api)
          }
        }
    }
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.android",
      "org.jetbrains.kotlin.jvm",
    ) {
      project.dependencies.add(
        "implementation",
        ktProvider.api.replace("provider-api", "provider-api-jvm")
      )
    }
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.js",
      "kotlin2js",
    ) {
      project.dependencies.add(
        "implementation",
        ktProvider.api.replace("provider-api", "provider-api-js")
      )
    }
  }
  
  // Generate the implementation class of KtProviderInitializer.
  private fun configCreateKtProviderTask(ktProvider: KtProviderExtensions): TaskProvider<Task> {
    val ktProviderRouterPackageName = KtProviderExtensions.getPackageName(project)
    val ktProviderRouterClassName = "${KtProviderExtensions.getClassNameSuffix(project)}KtProviderRouter"
    val selfInitializerClass = KtProviderExtensions.getInitializerClass(project)
    val initializerPackageName = selfInitializerClass.substringBeforeLast(".")
    val initializerClassName = selfInitializerClass.substringAfterLast(".")
    project.extensions.configure(KspExtension::class.java) {
      it.arg("ktProviderRouterPackageName", ktProviderRouterPackageName)
      it.arg("ktProviderRouterClassName", ktProviderRouterClassName)
      it.arg("ktProviderInitializerPackageName", initializerPackageName)
      it.arg("ktProviderInitializerClassName", initializerClassName)
    }
    return project.tasks.register(taskName) { task ->
      task.group = "ktProvider"
      val dependModuleProjects = getDependModulePaths(ktProvider)
      task.inputs.property("dependModulePaths", dependModuleProjects.map { it.path })
      task.inputs.property("configurations", ktProvider.configurations)
      task.outputs.dir(ktProviderSource)
      task.doLast {
        val text = getKtProviderInitializerTemplate(
          initializerPackageName,
          initializerClassName,
          dependModuleProjects.filter { dependProject ->
            dependProject.extensions.findByName("ktProvider") != null
          }.map { dependProject ->
            KtProviderExtensions.getInitializerClass(dependProject)
          },
        )
        outputFile(selfInitializerClass, it.name, text)
      }
    }
  }
  
  private fun configSourceSetSrcDir(taskProvider: TaskProvider<Task>) {
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      project.extensions
        .getByType(KotlinSourceSetContainer::class.java)
        .sourceSets
        .named("commonMain") {
          it.kotlin.srcDirs(taskProvider)
        }
    }
    project.plugins.withAnyId(
      "org.jetbrains.kotlin.android",
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.js",
      "kotlin2js",
    ) {
      project.extensions
        .getByType(KotlinSourceSetContainer::class.java)
        .sourceSets
        .named("main") {
          it.kotlin.srcDirs(taskProvider)
        }
    }
  }
  
  private fun PluginContainer.withAnyId(vararg ids: String, action: Action<in Plugin<*>>) {
    ids.forEach { withId(it, action) }
  }
  
  // Retrieve the paths of all dependent modules.
  private fun getDependModulePaths(ktProvider: KtProviderExtensions): List<Project> {
    val dependProjects = mutableListOf<Project>()
    ktProvider.configurations.mapNotNull {
      project.configurations.findByName(it)
    }.forEach { config ->
      config.dependencies.forEach { dependency ->
        if (dependency is ProjectDependency) {
          val dependProject = dependency.dependencyProject
          dependProjects.add(dependProject)
        }
      }
    }
    return dependProjects
  }
  
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
      "// Automatically generated for Gradle task: $taskName \n" +
          fileText
    )
  }
}

private fun getKtProviderInitializerTemplate(
  initializerPackageName: String,
  initializerClassName: String,
  invokeInitializerClass: List<String>,
): String = """
  package $initializerPackageName
  
  import com.g985892345.provider.api.init.KtProviderInitializer
  import com.g985892345.provider.api.init.KtProviderRouter
  
  object $initializerClassName : KtProviderInitializer() {
  
    override val router: KtProviderRouter = getRouter()
    
    override val otherModuleKtProvider: List<KtProviderInitializer> = listOf(
      ${invokeInitializerClass.joinToString(",\n      ")}
    )
  }
  
  // KSP will generate functions with the same name but fewer arguments,
  // which will have a higher priority
  @Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
  private fun $initializerClassName.getRouter(
    a0: Unit = Unit,
    a1: Unit = Unit,
    a2: Unit = Unit,
    a3: Unit = Unit,
    a4: Unit = Unit,
    a5: Unit = Unit,
    a6: Unit = Unit,
  ): KtProviderRouter {
    return KtProviderRouter.Empty
  }
""".trimIndent()
