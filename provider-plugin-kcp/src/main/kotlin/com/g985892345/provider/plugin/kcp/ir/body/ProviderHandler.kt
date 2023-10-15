package com.g985892345.provider.plugin.kcp.ir.body

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 23:45
 */
interface ProviderHandler {
  
  /**
   * 初始化时设置
   */
  fun init(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    messageCollector: MessageCollector,
  )
  
  /**
   * 遍历所有 class 时回调
   *
   * 子类需要在这里面保存符合要求的 class，然后在 [generateCode] 方法中使用
   */
  fun selectIrClass(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    irClass: IrClass
  )
  
  /**
   * 生成代码时回调
   *
   * @param initImplFunction _initImpl 方法
   * @param ktProviderInitializer IKtProviderInitializer 接口
   * @param ktProviderInitializerImpl IKtProviderInitializer 接口的实现类
   */
  fun IrBlockBodyBuilder.generateCode(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    initImplFunction: IrSimpleFunction,
    ktProviderInitializer: IrClassSymbol,
    ktProviderInitializerImpl: IrClass,
  )
}