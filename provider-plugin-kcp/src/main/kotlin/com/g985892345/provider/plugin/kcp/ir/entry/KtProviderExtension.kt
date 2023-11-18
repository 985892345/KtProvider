package com.g985892345.provider.plugin.kcp.ir.entry

import com.g985892345.provider.plugin.kcp.ir.body.kclass.KClassProviderHandler
import com.g985892345.provider.plugin.kcp.ir.body.impl.ImplProviderHandler
import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 23:08
 */
class KtProviderExtension(
  private val data: KtProviderData
) : IrGenerationExtension {
  
  private val handlers = listOf(
    ImplProviderHandler(data),
    KClassProviderHandler(data),
  )
  
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    log("KtProvider init")
    val ktProviderInitializerClassId =
      ClassId(FqName("com.g985892345.provider.init"), FqName("KtProviderInitializer"), false)
    // KtProviderInitializer 声明
    val ktProviderInitializerSymbol = pluginContext.referenceClass(ktProviderInitializerClassId)!!
    // 初始化所有 handler
    handlers.forEach {
      it.init(pluginContext, moduleFragment)
    }
    moduleFragment.files.forEach { irFile ->
      irFile.acceptChildrenVoid(object : IrElementVisitorVoid {
        override fun visitClass(declaration: IrClass) {
          super.visitClass(declaration)
          handlers.forEach {
            it.selectIrClass(pluginContext, moduleFragment, declaration)
          }
        }
      })
    }
    val initializerImplClassId = ClassId(
      FqName(data.initializerClass.substringBeforeLast(".")),
      FqName(data.initializerClass.substringAfterLast(".")),
      false
    )
    val initializerImplSymbol = pluginContext.referenceClass(initializerImplClassId)
      ?: throw IllegalArgumentException("未找到 KtProviderInitializer 的实现类: ${data.initializerClass}")
    if (!initializerImplSymbol.isSubtypeOfClass(ktProviderInitializerSymbol)) {
      throw IllegalStateException("${data.initializerClass} 不是 KtProviderInitializer 的实现类")
    }
    val initializerImplIrClass = initializerImplSymbol.owner
    // 添加 _initImpl 方法
    val initImplFunction =
      addInitImplFunction(pluginContext, moduleFragment, ktProviderInitializerSymbol, initializerImplIrClass)
    val superInitFunction =
      ktProviderInitializerSymbol.owner.functions.single { it.name.asString() == "initAddAllProvider" }
    // 修改 initAddAllProvider 方法
    overrideInitProviderFunction(pluginContext, initializerImplIrClass, superInitFunction, initImplFunction)
  }
  
  private fun addInitImplFunction(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    ktProviderInitializer: IrClassSymbol,
    ktProviderInitializerImpl: IrClass,
  ): IrSimpleFunction {
    return ktProviderInitializerImpl.addFunction(
      "_initImpl",
      pluginContext.irBuiltIns.unitType,
      modality = Modality.FINAL,
      visibility = DescriptorVisibilities.PRIVATE
    ).also { initImplFun ->
      initImplFun.body = DeclarationIrBuilder(pluginContext, initImplFun.symbol).irBlockBody {
        handlers.forEach { handler ->
          handler.apply {
            generateCode(
              pluginContext,
              moduleFragment,
              initImplFun,
              ktProviderInitializer,
              ktProviderInitializerImpl
            )
          }
        }
      }
    }
  }
  
  private fun overrideInitProviderFunction(
    pluginContext: IrPluginContext,
    declaration: IrClass,
    superInitFunction: IrSimpleFunction,
    initImplFunction: IrSimpleFunction
  ) {
    val initFun = declaration.functions.single { it.overrides(superInitFunction) }
    if (initFun.isFakeOverride) {
      // 如果没有重写 initAddAllProvider 方法
      declaration.declarations.remove(initFun)
      declaration.addFunction(
        initFun.name.asString(),
        pluginContext.irBuiltIns.unitType,
        modality = initFun.modality,
        visibility = initFun.visibility
      ).also { newInitFun ->
        newInitFun.overriddenSymbols += initFun.overriddenSymbols
        newInitFun.body = DeclarationIrBuilder(pluginContext, newInitFun.symbol).irBlockBody {
          // 插入调用 _initImpl 方法的逻辑
          +irCall(initImplFunction).also {
            it.dispatchReceiver = irGet(newInitFun.dispatchReceiverParameter!!)
          }
        }
      }
    } else {
      // 重写了 initAddAllProvider 方法
      initFun.body = DeclarationIrBuilder(pluginContext, initFun.symbol).irBlockBody {
        // 插入调用 _initImpl 方法的逻辑
        +irCall(initImplFunction).also {
          it.dispatchReceiver = irGet(initFun.dispatchReceiverParameter!!)
        }
        // 原有的代码逻辑
        initFun.body?.statements?.forEach {
          +it
        }
      }
    }
  }
  
  private fun log(mes: String?) {
    data.message.log(mes ?: "null")
  }
}