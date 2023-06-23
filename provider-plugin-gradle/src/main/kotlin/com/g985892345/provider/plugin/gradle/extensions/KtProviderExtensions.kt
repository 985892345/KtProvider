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
   * - *: 匹配任意长度字符，但不含包名分隔符
   * - **: 匹配任意长度字符，包含包名分隔符
   * ```kotlin
   * ktProvider {
   *     packageName {
   *         include("a.b")    // 匹配 a.b 下面的所有包和类
   *     }
   * }
   * ```
   *
   * ## 注：
   * include 与 exclude 的调用先后顺序不具有交换律，可以根据先后顺序的不同得出不同的结果
   */
  fun packageName(packageName: Action<PackageNameManager>) {
    packageName.execute(packageNameManager)
  }
}