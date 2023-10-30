package com.g985892345.provider.plugin.kcp.cache

import java.io.File

/**
 * .
 *
 * @author 985892345
 * 2023/10/17 17:17
 */
data class CacheManagerDir(
  val cacheDir: File
) {
  
  fun resolveDir(relative: String): CacheManagerDir {
    val file = cacheDir.resolve(relative)
    if (!file.exists()) file.mkdirs() else {
      if (!file.isDirectory) {
        throw IllegalArgumentException("$file 不是文件夹")
      }
    }
    return CacheManagerDir(file)
  }
  
  fun resolveFile(relative: String): CacheManagerFile {
    val file = cacheDir.resolve(relative)
    if (!file.exists()) file.createNewFile() else {
      if (!file.isFile) {
        throw IllegalArgumentException("$file 不是文件")
      }
    }
    return CacheManagerFile(file)
  }
}


data class CacheManagerFile(
  val file: File
) {
  
  fun put(value: String) {
    file.writeText(value)
  }
  
  fun get(): String {
    return file.readText()
  }
}