package com.g985892345.provider.plugin.kcp.ir.base

import com.g985892345.provider.plugin.kcp.ir.body.kclass.KClassProviderHandler
import com.g985892345.provider.plugin.kcp.ir.body.impl.NewImplProviderHandler
import com.g985892345.provider.plugin.kcp.ir.body.impl.SingleImplProviderHandler
import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 23:08
 */
class KtProviderExtension(
  private val message: MessageCollector,
  private val packages: List<String>,
  private val isCheckImpl: Boolean,
) : IrGenerationExtension {
  
  private val handlers = listOf(
    SingleImplProviderHandler(isCheckImpl),
    NewImplProviderHandler(isCheckImpl),
    KClassProviderHandler(),
  )
  
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    log("KtProvider init")
    var isFound = false
    val ktProviderInitializerClassId =
      ClassId(FqName("com.g985892345.provider.init"), FqName("KtProviderInitializer"), false)
    // KtProviderInitializer 接口
    val ktProviderInitializerSymbol = pluginContext.referenceClass(ktProviderInitializerClassId)!!
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        // 筛选出 object 单例类或者是非抽象的 class
        val modality = declaration.modality
        if (declaration.isObject || declaration.isClass && (modality == Modality.OPEN || modality == Modality.FINAL)) {
          // 检查是否是 KtProviderInitializer 的实现类
          if (declaration.isSubclassOf(ktProviderInitializerSymbol.owner)) {
            if (isFound) throw IllegalStateException("存在多个 KtProviderInitializer 的实现类")
            isFound = true
            // 初始化所有 handler
            handlers.forEach {
              it.init(
                moduleFragment,
                pluginContext,
                ktProviderInitializerSymbol,
                declaration,
                message
              )
            }
            // 添加 _initImpl 方法
            val initImplFunction = addInitImplFunction(pluginContext, declaration)
            val superInitFunction =
              ktProviderInitializerSymbol.owner.functions.single { it.name.asString() == "initKtProvider" }
            // 修改 initKtProvider 方法
            overrideInitProviderFunction(pluginContext, declaration, superInitFunction, initImplFunction)
          }
        }
        return super.visitClass(declaration)
      }
    })
    if (!isFound) {
      throw RuntimeException("该模块未找到 KtProviderInitializer 的实现类，请检查 KtProvider 插件配置是否正确   module=${moduleFragment.name}")
    }
  }
  
  private fun addInitImplFunction(
    pluginContext: IrPluginContext,
    declaration: IrClass,
  ): IrSimpleFunction {
    return declaration.addFunction(
      "_initImpl",
      pluginContext.irBuiltIns.unitType,
      modality = Modality.FINAL,
      visibility = DescriptorVisibilities.PRIVATE
    ).also { initImplFun ->
      initImplFun.body = DeclarationIrBuilder(pluginContext, initImplFun.symbol).irBlockBody {
        val allClassDescriptor = findAllClassDescriptors(pluginContext)
        allClassDescriptor.forEach { classDescriptor ->
          handlers.forEach { handler ->
            handler.apply {
              // 交给 handler 去处理 _initImpl 方法
              processInitImplFunction(pluginContext, initImplFun, classDescriptor)
            }
          }
        }
      }
    }
  }
  
  // 查找所有 ClassDescriptor，包含依赖
  private fun findAllClassDescriptors(
    pluginContext: IrPluginContext
  ): Sequence<ClassDescriptor> {
    fun findClassDescriptor(descriptor: PackageViewDescriptor): Sequence<ClassDescriptor> {
      return descriptor.memberScope.getContributedDescriptors()
        .asSequence()
        .map {
          when (it) {
            is ClassDescriptor -> sequenceOf(it)
            is PackageViewDescriptor -> findClassDescriptor(it)
            else -> emptySequence()
          }
        }.flatten()
    }
    
    val newPackage = if (packages.isNotEmpty()) packages else listOf("")
    return newPackage.asSequence()
      .map {
        val descriptor = pluginContext.moduleDescriptor
          .getPackage(FqName(it))
        findClassDescriptor(descriptor)
      }.flatten()
  }
  
  private fun overrideInitProviderFunction(
    pluginContext: IrPluginContext,
    declaration: IrClass,
    superInitFunction: IrSimpleFunction,
    initImplFunction: IrSimpleFunction
  ) {
    val initFun = declaration.functions.single { it.overrides(superInitFunction) }
    if (initFun.isFakeOverride) {
      // 如果没有重写 initKtProvider 方法
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
      // 重写了 initKtProvider 方法
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
  
  private fun log(mes: String) {
    message.log(mes)
  }
}