package com.g985892345.provider.plugin.gradle.extensions

import org.gradle.api.Project
import java.io.Serializable

/**
 * .
 *
 * @author 985892345
 * 2023/6/18 12:36
 */
abstract class KtProviderExtensions(private val project: Project) {
  
  /**
   * 是否启用 Kcp 功能，对 KtProviderInitializer 进行 ir 插桩
   *
   * 如果不启用的话，则只包含自动生成 KtProviderInitializer 实现类的功能
   */
  var isApplyKcp = true
  
  /**
   * 是否检查被注解类是注解中标注参数的实现类
   * ```
   * 比如：
   * @NewImplProvider(clazz = Parent::class)
   * class Test : ITest
   * ```
   * 在开启此选项后，如果 Test 不是 Parent 的实现类时则会在编译期报错，
   * 如果未开启则会在运行时发生强转错误
   */
  var isCheckImpl = true
  
  /**
   * 自动生成的 KtProviderInitializer 实现类名字
   */
  var initializerClassName = "${project.name.capitalized()}KtProviderInitializer"
  
  /**
   * 自动生成的 KtProviderInitializer 实现类包名
   */
  var initializerClassPackage = getInitializerClassPackageByProject()
  
  // 包名 + 类名 + 方法名，initProvider() 中前插入
  internal val mBeforeFunctions = mutableListOf<Function>()
  
  // 包名 + 类名 + 方法名，initProvider() 中前插入
  internal val mAfterFunctions = mutableListOf<Function>()
  
  /**
   * 在 initProvider() 方法体开头插入一个静态方法或者 object 单例类方法
   *
   * 并不一定是方法体的开头，如果 [isApplyKcp] 为 true，在开启了对 KtProviderInitializer 实现类 ir 插桩时，
   * 则用于初始化路由的方法将在最前面，紧接着是该函数设置的方法。
   */
  fun beforeFunction(packageName: String, className: String, functionName: String) {
    mBeforeFunctions.add(Function(packageName, className, functionName))
  }
  
  /**
   * 在 initProvider() 方法体末尾插入一个静态方法或者 object 单例类方法
   */
  fun afterFunction(packageName: String, className: String, functionName: String) {
    mAfterFunctions.add(Function(packageName, className, functionName))
  }
  
  
  /**
   * 内部使用，是否已经被关联
   */
  internal var isLinked = false
  
  private fun String.capitalized(): String {
    return replaceFirstChar {
      it.uppercaseChar()
    }
  }
  
  private fun getInitializerClassPackageByProject(): String {
    val prefix = "com.g985892345.provider."
    var name = project.name
    var p = project
    while (p.parent != null) {
      p = p.parent!!
      val projectNamePackage = p.name.replace(Regex("[^0-9a-zA-Z]"), "")
      name = "${projectNamePackage}.$name"
    }
    name = name.lowercase()
    return prefix + name
  }
  
  internal class Function(
    val packageName: String,
    val className: String,
    val functionName: String,
  ) : Serializable {
    val import = "import $packageName.$className"
    val invoke = "$className.$functionName()"
  }
}