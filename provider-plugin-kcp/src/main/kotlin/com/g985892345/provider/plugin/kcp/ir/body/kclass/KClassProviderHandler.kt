package com.g985892345.provider.plugin.kcp.ir.body.kclass

import com.g985892345.provider.plugin.kcp.cache.IrClassCacheData
import com.g985892345.provider.plugin.kcp.ir.body.BaseClazzNameProviderHandler
import com.g985892345.provider.plugin.kcp.ir.body.utils.ClazzNameCacheManager
import com.g985892345.provider.plugin.kcp.ir.body.utils.ClazzNameProviderArg
import com.g985892345.provider.plugin.kcp.ir.body.utils.toClazzNameProviderArg
import com.g985892345.provider.plugin.kcp.ir.entry.KtProviderData
import com.g985892345.provider.plugin.kcp.ir.utils.location
import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
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

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 15:53
 */
class KClassProviderHandler(
  data: KtProviderData
) : BaseClazzNameProviderHandler(data) {
  
  private val mCacheManager = ClazzNameCacheManager(
    data.cacheManagerDir.resolveFile("KClassProviderCache.json")
  )
  
  private val kClassProviderAnnotation = FqName("com.g985892345.provider.annotation.KClassProvider")
  private val mGenerateArgByClassId = LinkedHashMap<ClassId, Pair<IrClass, List<ClazzNameProviderArg>>>()
  
  override fun init(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment) {
    super.init(pluginContext, moduleFragment)
    mGenerateArgByClassId.clear()
    // 加载缓存
    mCacheManager.get()
      .mapNotNull { data ->
        pluginContext.referenceClass(data.impl.classId)?.let { it to data }
      }
      .forEach {
        messageCollector.log("KClassProviderCache: class=${it.first.owner.location}")
        val classId = it.first.owner.classId!!
        mGenerateArgByClassId[classId] = it.first.owner to it.second.toClazzNameProviderArg(pluginContext)
      }
  }
  
  override fun selectIrClass(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment, irClass: IrClass) {
    val classId = irClass.classId ?: return
    if (irClass.isClass || irClass.isInterface || irClass.isObject) {
      val annotations = irClass.annotations.mapNotNull {
        if (it.isAnnotation(kClassProviderAnnotation)) it else null
      }
      if (annotations.isNotEmpty()) {
        // 如果 classId 相同，这里会覆盖掉缓存
        mGenerateArgByClassId[classId] = irClass to annotations.map {
          getImplProviderArg(irClass, it)
        }
      } else {
        // 如果 annotations 为空了，mIrClassWithAnnotation 仍包含 classId，说明是缓存添加的, 所以尝试移除 classId
        mGenerateArgByClassId.remove(classId)
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
    val superClassFunction = ktProviderInitializer.owner
      .functions
      .single {
        it.name.asString() == "addKClassProvider"
      }
    val addKClassProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
    mGenerateArgByClassId.forEach { entry ->
      val irClass = entry.value.first
      entry.value.second.forEach {
        messageCollector.log("@KClassProvider: $irClass.location -> (${it.msg})")
        +irAddKClassProvider(pluginContext, it, irClass.symbol, initImplFunction, addKClassProviderFunction)
      }
    }
    
    // 保存进缓存
    mCacheManager.put(
      mGenerateArgByClassId.map { entry ->
        ClazzNameCacheManager.ClazzNameCacheData(
          IrClassCacheData(entry.key),
          entry.value.second.map { arg ->
            arg.irClass?.classId?.let { IrClassCacheData(it) } to arg.name
          }
        )
      }
    )
  }
  
  private fun IrBuilderWithScope.irAddKClassProvider(
    pluginContext: IrPluginContext,
    arg: ClazzNameProviderArg,
    classSymbol: IrClassSymbol,
    initImplFunction: IrSimpleFunction,
    addKClassProviderFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(addKClassProviderFunction).also { call ->
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
      call.putValueArgument(
        2,
        IrFunctionExpressionImpl(
          startOffset, endOffset,
          pluginContext.irBuiltIns.functionN(0).typeWith(pluginContext.irBuiltIns.kClassClass.starProjectedType),
          initImplFunction.factory.buildFun {
            name = Name.special("<anonymous>")
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            visibility = DescriptorVisibilities.LOCAL
            returnType = pluginContext.irBuiltIns.kClassClass.starProjectedType
          }.also { lambda ->
            lambda.setDeclarationsParent(initImplFunction)
            lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
              +irReturn(
                IrClassReferenceImpl(
                  startOffset, endOffset,
                  pluginContext.irBuiltIns.kClassClass.typeWith(classSymbol.defaultType),
                  classSymbol,
                  classSymbol.defaultType
                )
              )
            }
          },
          IrStatementOrigin.LAMBDA
        )
      )
    }
  }
}