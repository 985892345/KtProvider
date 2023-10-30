package com.g985892345.provider.plugin.kcp.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * .
 *
 * @author 985892345
 * 2023/10/30 00:11
 */
@Serializable
data class IrClassCacheData(
  val packageName: String,
  val className: String,
) {
  
  constructor(classId: ClassId) : this(classId.packageFqName.asString(), classId.relativeClassName.asString())
  
  @Transient
  val classId = ClassId(FqName(packageName), Name.identifier(className))
}