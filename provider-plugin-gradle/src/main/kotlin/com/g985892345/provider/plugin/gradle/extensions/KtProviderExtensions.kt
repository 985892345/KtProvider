package com.g985892345.provider.plugin.gradle.extensions

import com.g985892345.provider.plugin.gradle.extensions.name.PackageNameManager
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * 2023/6/18 12:36
 */
abstract class KtProviderExtensions(private val project: Project) {
  
  internal val packageNameManager = PackageNameManager()
  
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
  val isCheckImpl = true
  
  /**
   * 设置包名
   * ```kotlin
   * ktProvider {
   *     packageName {
   *         include("a.b")    // 匹配 a.b 下面的所有包及子包中的类
   *     }
   * }
   * ```
   */
  fun packageName(packageName: Action<PackageNameManager>) {
    packageName.execute(packageNameManager)
  }
}