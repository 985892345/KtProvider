package com.g985892345.provider.plugin.kcp.ir.body

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 23:45
 */
sealed interface ProviderHandler {
  
  fun init(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    providerInitializerClass: IrClass,
    messageCollector: MessageCollector
  )
  
  fun IrBlockBodyBuilder.processInitImplFunction(
    pluginContext: IrPluginContext,
    initImplFunction: IrSimpleFunction,
    descriptor: ClassDescriptor,
  )
  
  companion object {
    private val UniqueKey = hashSetOf<String>()
    
    fun checkUniqueKey(key: String, msg: (() -> String)? = null) {
      if (UniqueKey.contains(key)) {
        throw IllegalArgumentException(msg?.invoke() ?: "已包含重复的 key")
      }
    }
  }
}