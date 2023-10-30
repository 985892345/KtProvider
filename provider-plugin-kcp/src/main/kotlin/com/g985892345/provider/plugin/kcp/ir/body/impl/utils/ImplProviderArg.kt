package com.g985892345.provider.plugin.kcp.ir.body.impl.utils

import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass

/**
 * 注解内部参数
 *
 * @author 985892345
 * 2023/10/30 10:41
 */
data class ImplProviderArg(
  val classReference: IrClassReference?,
  val name: String?
) {
  // 打印信息
  val msg = when {
    classReference == null && name == null -> ""
    classReference == null && name != null -> "name=$name"
    classReference != null && name == null -> "clazz=${classReference.classType.classFqName}"
    classReference != null && name != null -> "clazz=${classReference.classType.classFqName}, name=$name"
    else -> error("")
  }
  
  val irClass = classReference?.classType?.getClass()
}