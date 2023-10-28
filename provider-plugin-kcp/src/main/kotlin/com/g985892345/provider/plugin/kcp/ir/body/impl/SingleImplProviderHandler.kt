package com.g985892345.provider.plugin.kcp.ir.body.impl

import com.g985892345.provider.plugin.kcp.cache.ClassIdCacheManager
import com.g985892345.provider.plugin.kcp.ir.entry.KtProviderData
import com.g985892345.provider.plugin.kcp.ir.utils.location
import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.classId

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 15:48
 */
class SingleImplProviderHandler(
  data: KtProviderData
) : BaseImplProviderHandler(data) {
  
  private val mClassIdCache = ClassIdCacheManager(
    data.cacheManagerDir.resolveFile("SingleImplProviderCache.json")
  )
  
  private val singleImplProviderAnnotation = FqName("com.g985892345.provider.annotation.SingleImplProvider")
  private val mIrClassWithAnnotation = LinkedHashMap<ClassId, Pair<IrClass, List<IrConstructorCall>>>()
  
  override fun init(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment) {
    super.init(pluginContext, moduleFragment)
    mIrClassWithAnnotation.clear()
    // 加载缓存
    mClassIdCache.get()
      .mapNotNull { pluginContext.referenceClass(it) }
      .forEach {
        messageCollector.log("SingleImplProviderCache: class=${it.owner.location}")
        selectIrClass(pluginContext, moduleFragment, it.owner)
      }
  }
  
  override fun selectIrClass(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment, irClass: IrClass) {
    val classId = irClass.classId ?: return
    val kind = irClass.kind
    val modality = irClass.modality
    // 普通 class 和 object 单例
    if (kind.isClass && (modality == Modality.OPEN || modality == Modality.FINAL) || kind.isObject) {
      val annotations = irClass.annotations.mapNotNull {
        if (it.isAnnotation(singleImplProviderAnnotation)) it else null
      }
      if (annotations.isNotEmpty()) {
        // 如果 classId 相同，这里会覆盖掉缓存
        mIrClassWithAnnotation[classId] = irClass to annotations
      } else {
        // 如果 annotations 为空了，mIrClassWithAnnotation 仍包含 classId，说明是缓存添加的, 所以尝试移除 classId
        mIrClassWithAnnotation.remove(classId)
      }
    }
  }
  
  override fun IrBlockBodyBuilder.generateCode(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    initImplFunction: IrSimpleFunction,
    ktProviderInitializer: IrClassSymbol,
    ktProviderInitializerImpl: IrClass
  ) {
    messageCollector.log("generateCode")
    val superClassFunction = ktProviderInitializer.owner
      .functions
      .single {
        it.name.asString() == "addSingleImplProvider"
      }
    val implProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
    mIrClassWithAnnotation.forEach { entry ->
      val irClass = entry.value.first
      entry.value.second.forEach { annotation ->
        checkEmptyConstructor(irClass)
        val arg = getImplProviderArg(irClass, annotation)
        checkImpl(irClass, arg.classReference)
        messageCollector.log("@SingleImplProvider: ${irClass.location} -> (${arg.msg})")
        +irAddSingleImplProvider(pluginContext, arg, irClass, initImplFunction, implProviderFunction)
      }
    }
    
    // 保存进缓存
    mClassIdCache.put(
      mIrClassWithAnnotation.mapNotNullTo(hashSetOf()) {
        it.key
      }
    )
  }
  
  private fun IrBuilderWithScope.irAddSingleImplProvider(
    pluginContext: IrPluginContext,
    arg: ImplProviderArg,
    irClass: IrClass,
    initImplFunction: IrSimpleFunction,
    implProviderFunction: IrSimpleFunction
  ): IrExpression {
    return irCall(implProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      // 添加 KClass 参数
      // classReference 为 null 时默认填充 Nothing::class，因为 Nothing 无实现类
      call.putValueArgument(
        0,
        arg.classReference ?: IrClassReferenceImpl(
          startOffset, endOffset,
          context.irBuiltIns.kClassClass.starProjectedType,
          context.irBuiltIns.kClassClass,
          nothingSymbol.defaultType
        )
      )
      // 添加 name 参数
      call.putValueArgument(1, irString(arg.name ?: ""))
      // 添加 init 参数
      call.putValueArgument(
        2,
        IrFunctionExpressionImpl(
          UNDEFINED_OFFSET, UNDEFINED_OFFSET,
          pluginContext.irBuiltIns.functionN(0).typeWith(pluginContext.irBuiltIns.anyType),
          initImplFunction.factory.buildFun {
            name = Name.special("<anonymous>")
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
            returnType = pluginContext.irBuiltIns.anyType
          }.also { lambda ->
            lambda.setDeclarationsParent(initImplFunction)
            lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
              +irReturn(
                if (irClass.isObject) {
                  irGetObject(irClass.symbol)
                } else {
                  irCall(irClass.constructors.single { it.valueParameters.isEmpty() })
                }
              )
            }
          },
          IrStatementOrigin.LAMBDA
        )
      )
    }
  }
}