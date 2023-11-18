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
  var enableKcp = true
  
  /**
   * KtProviderInitializerClass 的代理类
   *
   * 设置后自动生成的 KtProviderInitializer 实现类将调用代理类的方法
   *
   * 代理类要求如下:
   * - object 单例或者 companion 伴生对象的静态方法
   * - 修饰符可以设置成 public 或者 internal
   * - 实现 IKtProviderInitializerDelegate 接口
   */
  var delegateClass: String? = null
  
  /**
   * ir 插桩后的缓存信息，用于保存开启增量编译后需要编译信息
   */
  var cachePath = project.layout.buildDirectory.dir(
    "generated/source/ktProvider/cache"
  )
  
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
  
  
  companion object {
    
    /**
     * 得到自动生成的 KtProviderInitializer 实现类全称
     */
    fun getInitializerClass(project: Project): String {
      val prefix = "com.g985892345.provider."
      var packageName = project.name
      var p = project
      while (p.parent != null) {
        p = p.parent!!
        val projectNamePackage = p.name
        packageName = "${projectNamePackage}.$packageName"
      }
      packageName = prefix + packageName.lowercase().replace(Regex("[^0-9a-zA-Z.]"), "")
      return "$packageName." + project.name
        .split(Regex("[^0-9a-zA-Z]"))
        .joinToString("") { it.capitalized() } + "KtProviderInitializer"
    }
    
    private fun String.capitalized(): String {
      return replaceFirstChar {
        it.uppercaseChar()
      }
    }
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