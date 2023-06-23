package com.g985892345.provider.plugin.gradle.extensions.name

/**
 * .
 *
 * @author 985892345
 * 2023/6/18 11:11
 */
class PackageNameManager {
  
  internal val nameMatcher = mutableListOf<String>()
  
  fun include(name: String) {
    nameMatcher.add(name)
  }
}