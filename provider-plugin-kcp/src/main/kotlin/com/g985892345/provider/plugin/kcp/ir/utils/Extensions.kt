package com.g985892345.provider.plugin.kcp.ir.utils

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.classId

/**
 * .
 *
 * @author 985892345
 * 2023/10/18 15:34
 */

val IrClass.location: String
  get() = classId!!.asFqNameString()

val IrClassSymbol.location: String
  get() = owner.location