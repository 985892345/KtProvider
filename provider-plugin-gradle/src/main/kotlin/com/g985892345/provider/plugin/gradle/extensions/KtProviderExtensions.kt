package com.g985892345.provider.plugin.gradle.extensions

import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * 2023/6/18 12:36
 */
abstract class KtProviderExtensions(private val project: Project) {
  
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
   * 是否自动生成 IKtProviderInitializer 的实现类
   *
   * KtProvider 的 gradle 插件会自动生成一个 IKtProviderInitializer 的实现类，
   * 并且会根据该模块的依赖关系自动调用其他模块的 initProvider() 方法
   *
   * 如果你不打算自动生成，则需要自动去调用其他模块的 initProvider()
   *
   * true -> 自动生成 IKtProviderInitializer 的实现类，自动被其他模块关联
   * false -> 不自动生成 IKtProviderInitializer 的实现类，自动被其他模块关联
   * null -> 不自动生成 IKtProviderInitializer 的实现类，不会被其他模块关联
   */
  var isAutoCreateKtProviderInitializer: Boolean? = true
  
  /**
   * 自动生成的 IKtProviderInitializer 实现类名字
   *
   * 注意: 如果 [isAutoCreateKtProviderInitializer] 为 false，需要设置为自己的实现类
   */
  var initializerClassName = "${project.name.capitalized()}KtProviderInitializer"
  
  /**
   * 自动生成的 IKtProviderInitializer 实现类包名
   *
   * 注意: 如果 [isAutoCreateKtProviderInitializer] 为 false，需要设置为自己的实现类
   */
  var initializerClassPackage = getInitializerClassPackageByProject()
  
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
    return prefix + name
  }
}