package com.g985892345.provider.plugin.kcp.ir.body.kclass

import com.g985892345.provider.plugin.kcp.cache.ClassIdCacheManager
import com.g985892345.provider.plugin.kcp.ir.body.ProviderHandler
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
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
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
) : ProviderHandler {
  
  private val mClassIdCache = ClassIdCacheManager(
    data.cacheManagerDir.resolveFile("KClassProviderCache.json")
  )
  
  private val kClassProviderAnnotation = FqName("com.g985892345.provider.annotation.KClassProvider")
  private val mIrClassWithAnnotation = LinkedHashMap<ClassId, Pair<IrClass, List<IrConstructorCall>>>()
  private val messageCollector = data.message
  
  override fun init(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment) {
    mIrClassWithAnnotation.clear()
    // 加载缓存
    mClassIdCache.get()
      .mapNotNull { pluginContext.referenceClass(it) }
      .forEach {
        messageCollector.log("KClassProviderCache: class=${it.owner.location}")
        selectIrClass(pluginContext, moduleFragment, it.owner)
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
        mIrClassWithAnnotation[classId] = irClass to annotations
      } else {
        // 如果 annotations 为空了，mIrClassWithAnnotation 仍包含 classId，说明是缓存添加的, 所以尝试移除 classId
        mIrClassWithAnnotation.remove(classId)
      }
    }
  }
  
  @Suppress("UNCHECKED_CAST")
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
    mIrClassWithAnnotation.forEach { entry ->
      val irClass = entry.value.first
      entry.value.second.forEach { annotation ->
        val name = (annotation.getValueArgument(0) as IrConst<String>).value
        val key = getKey(irClass, name)
        putAndCheckUniqueKClassKey(key, irClass.location)
        messageCollector.log("@KClassProvider: $irClass.location")
        +irAddKClassProvider(pluginContext, key, irClass.symbol, initImplFunction, addKClassProviderFunction)
      }
    }
    
    // 保存进缓存
    mClassIdCache.put(
      mIrClassWithAnnotation.mapNotNullTo(hashSetOf()) {
        it.key
      }
    )
  }
  
  private fun IrBuilderWithScope.irAddKClassProvider(
    pluginContext: IrPluginContext,
    key: String,
    classSymbol: IrClassSymbol,
    initImplFunction: IrSimpleFunction,
    addKClassProviderFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(addKClassProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      val nameStr = key.toIrConst(pluginContext.irBuiltIns.stringType)
      call.putValueArgument(0, nameStr)
      call.putValueArgument(
        1,
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
  
  private fun getKey(irClass: IrClass, name: String?): String {
    if (name == null) {
      throw IllegalArgumentException("必须设置 name!   class=${irClass.location}")
    } else if (name.isEmpty()) {
      throw IllegalArgumentException("name 不能为空串!   class=${irClass.location}")
    }
    return name
  }
  
  private val IrClass.location: String
    get() = classId!!.asFqNameString()
  
  companion object {
    private val UniqueKey = hashMapOf<String, String>()
    
    fun putAndCheckUniqueKClassKey(key: String, locationMsg: String) {
      val lastLocationMsg = UniqueKey[key]
      if (lastLocationMsg != null) {
        throw IllegalArgumentException(
          "包含重复的声明: $key\n位置1: $lastLocationMsg\n位置2: $locationMsg"
        )
      } else {
        UniqueKey[key] = locationMsg
      }
    }
  }
}