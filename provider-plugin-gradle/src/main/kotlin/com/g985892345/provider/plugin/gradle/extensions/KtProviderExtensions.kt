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