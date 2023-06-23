package com.g985892345.provider.plugin.kcp.ir.body

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.isClass
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.classId

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 14:57
 */
class NewImplProviderHandler : ProviderHandler {
  
  private val newImplProviderAnnotation = FqName("com.g985892345.provider.annotation.NewImplProvider")
  private val clazzArg = Name.identifier("clazz")
  private val nameArg = Name.identifier("name")
  private lateinit var newImplProviderFunction: IrSimpleFunction
  private lateinit var messageCollector: MessageCollector
  
  override fun init(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    providerInitializerClass: IrClass,
    messageCollector: MessageCollector
  ) {
    this.messageCollector = messageCollector
    val superClassFunction = providerInitializerClass.superClass!!
      .functions
      .single {
        it.name.asString() == "addNewImplProvider"
      }
    newImplProviderFunction = providerInitializerClass.functions
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
    if (kind.isClass) {
      descriptor.annotations
        .filter { it.fqName == newImplProviderAnnotation }
        .forEach {
          descriptor.constructors.find { it.valueParameters.isEmpty() }
            ?: throw IllegalStateException("不存在空构造器 class=${descriptor.classId!!.asFqNameString()}")
          val kClass = it.allValueArguments[clazzArg]?.value as KClassValue.Value.NormalClass?
          val name = it.allValueArguments[nameArg]?.value as String?
          val key = getKey(kClass?.classId, name)
          ProviderHandler.checkUniqueKey(key) {
            val classIdArgMsg = if (kClass != null) "classId = " + kClass.classId.asFqNameString() else ""
            val nameArgMsg = if (name != null) "name = $name" else ""
            "已包含重复的申明: $classIdArgMsg   $nameArgMsg"
          }
          val irConstructor = pluginContext.referenceClass(descriptor.classId!!)!!
            .owner
            .constructors
            .find { it.valueParameters.isEmpty() }!!
          +irAddNewImplProvider(pluginContext, key, irConstructor, initImplFunction)
        }
    } else {
      throw IllegalStateException("@NewImplProvider 只能使用在 class 上")
    }
  }
  
  private fun IrBuilderWithScope.irAddNewImplProvider(
    pluginContext: IrPluginContext,
    key: String,
    emptyConstructor: IrConstructor,
    initImplFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(newImplProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      val nameStr = key.toIrConst(pluginContext.irBuiltIns.stringType)
      call.putValueArgument(0, nameStr)
      call.putValueArgument(
        1,
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
                irCall(emptyConstructor)
              )
            }
          },
          IrStatementOrigin.LAMBDA
        )
      )
    }
  }
  
  private fun getKey(classId: ClassId?, name: String?): String {
    return if (classId == null) {
      if (name.isNullOrEmpty()) {
        throw IllegalArgumentException("必须设置 clazz 或者 name!")
      } else {
        name
      }
    } else {
      if (name.isNullOrEmpty()) {
        classId.relativeClassName.asString()
      } else {
        classId.relativeClassName.asString() + "-$name"
      }
    }
  }
}