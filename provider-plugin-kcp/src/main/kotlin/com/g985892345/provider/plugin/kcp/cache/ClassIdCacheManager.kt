package com.g985892345.provider.plugin.kcp.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * .
 *
 * @author 985892345
 * 2023/10/18 12:45
 */
class ClassIdCacheManager(
  val cacheFile: CacheManagerFile
) {
  
  fun get(): List<ClassId> {
    val text = cacheFile.get()
    if (text.isEmpty()) return emptyList()
    return try {
      Json.decodeFromString<Set<IrClassCache>>(cacheFile.get())
        .map { it.classId }
    } catch (e: Exception) {
      emptyList()
    }
  }
  
  fun put(classIds: Set<ClassId>) {
    cacheFile.put(Json.encodeToString(classIds.map { IrClassCache(it) }))
  }
  
  @Serializable
  private data class IrClassCache(
    val packageName: String,
    val className: String,
  ) {
    
    constructor(classId: ClassId) : this(classId.packageFqName.asString(), classId.relativeClassName.asString())
    
    @Transient
    val classId = ClassId(FqName(packageName), Name.identifier(className))
  }
}