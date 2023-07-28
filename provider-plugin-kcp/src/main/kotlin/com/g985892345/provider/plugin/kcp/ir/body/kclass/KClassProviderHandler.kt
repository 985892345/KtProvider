package com.g985892345.provider.plugin.kcp.ir.body.kclass

import com.g985892345.provider.plugin.kcp.ir.body.ProviderHandler
import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
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
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.classId

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 15:53
 */
class KClassProviderHandler : ProviderHandler {
  
  private val kClassProviderAnnotation = FqName("com.g985892345.provider.annotation.KClassProvider")
  private val nameArg = Name.identifier("name")
  private lateinit var addKClassProviderFunction: IrSimpleFunction
  private lateinit var messageCollector: MessageCollector
  
  override fun init(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    ktProviderInitializer: IrClassSymbol,
    ktProviderInitializerImpl: IrClass,
    messageCollector: MessageCollector
  ) {
    this.messageCollector = messageCollector
    val superClassFunction = ktProviderInitializer.owner
      .functions
      .single {
        it.name.asString() == "addKClassProvider"
      }
    addKClassProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
  }
  
  override fun IrBlockBodyBuilder.processInitImplFunction(
    pluginContext: IrPluginContext,
    initImplFunction: IrSimpleFunction,
    descriptor: ClassDescriptor
  ) {
    val kind = descriptor.kind
    if (kind.isClass || kind.isInterface || kind.isObject) {
      descriptor.annotations
        .filter { it.fqName == kClassProviderAnnotation }
        .forEach {
          val classId = descriptor.classId!!
          val location = classId.asFqNameString()
          val name = it.allValueArguments[nameArg]?.value as String?
          val key = getKey(descriptor, name)
          putAndCheckUniqueKClassKey(key, location)
          messageCollector.log("@KClassProvider: $location")
          val classSymbol = pluginContext.referenceClass(classId)!!
          +irAddKClassProvider(pluginContext, key, classSymbol, initImplFunction)
        }
    }
  }
  
  private fun IrBuilderWithScope.irAddKClassProvider(
    pluginContext: IrPluginContext,
    key: String,
    classSymbol: IrClassSymbol,
    initImplFunction: IrSimpleFunction,
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
  
  private fun getKey(descriptor: ClassDescriptor, name: String?): String {
    if (name == null) {
      throw IllegalArgumentException("必须设置 name!   class=${descriptor.location}")
    } else if (name.isEmpty()) {
      throw IllegalArgumentException("name 不能为空串!   class=${descriptor.location}")
    }
    return name
  }
  
  private val ClassDescriptor.location: String
    get() = classId!!.asFqNameString()
  
  companion object {
    private val UniqueKey = hashMapOf<String, String>()
    
    fun putAndCheckUniqueKClassKey(key: String, locationMsg: String) {
      val lastLocationMsg = UniqueKey[key]
      if (lastLocationMsg != null) {
        throw IllegalArgumentException(
          "包含重复的申明: $key\n位置1: $lastLocationMsg\n位置2: $locationMsg"
        )
      } else {
        UniqueKey[key] = locationMsg
      }
    }
  }
}