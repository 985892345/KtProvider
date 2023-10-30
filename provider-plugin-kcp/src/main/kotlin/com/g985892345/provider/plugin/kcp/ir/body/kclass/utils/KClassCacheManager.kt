package com.g985892345.provider.plugin.kcp.ir.body.kclass.utils

import com.g985892345.provider.plugin.kcp.cache.CacheManagerFile
import com.g985892345.provider.plugin.kcp.cache.IrClassCacheData
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2023/10/30 12:31
 */
class KClassCacheManager(
  val cacheFile: CacheManagerFile
) {
  
  fun get(): List<KClassCacheData> {
    val text = cacheFile.get()
    if (text.isEmpty()) return emptyList()
    return try {
      Json.decodeFromString<List<KClassCacheData>>(cacheFile.get())
    } catch (e: Exception) {
      cacheFile.file.writeText("[]")
      emptyList()
    }
  }
  
  fun put(data: List<KClassCacheData>) {
    cacheFile.put(Json.encodeToString(data))
  }
  
  @Serializable
  data class KClassCacheData(
    val impl: IrClassCacheData,
    val names: List<String>,
  )
}