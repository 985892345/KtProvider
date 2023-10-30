package com.g985892345.provider.plugin.kcp.ir.body.impl.utils

import com.g985892345.provider.plugin.kcp.cache.CacheManagerFile
import com.g985892345.provider.plugin.kcp.cache.IrClassCacheData
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType

/**
 * .
 *
 * @author 985892345
 * 2023/10/30 00:05
 */
class ImplCacheManager(
  val cacheFile: CacheManagerFile
) {
  
  fun get(): List<ImplCacheData> {
    val text = cacheFile.get()
    if (text.isEmpty()) return emptyList()
    return try {
      Json.decodeFromString<List<ImplCacheData>>(cacheFile.get())
    } catch (e: Exception) {
      cacheFile.file.writeText("[]")
      emptyList()
    }
  }
  
  fun put(data: List<ImplCacheData>) {
    cacheFile.put(Json.encodeToString(data))
  }
  
  @Serializable
  data class ImplCacheData(
    val impl: IrClassCacheData,
    val annotation: List<Pair<IrClassCacheData?, String?>>,
  )
}

fun ImplCacheManager.ImplCacheData.toImplProviderArg(
  pluginContext: IrPluginContext,
): List<ImplProviderArg> {
  return annotation.map { pair ->
    ImplProviderArg(
      pair.first?.classId?.let { classId ->
        pluginContext.referenceClass(classId)?.let { symbol ->
          IrClassReferenceImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            symbol.starProjectedType,
            symbol,
            symbol.defaultType
          )
        }
      },
      pair.second
    )
  }
}
