package com.g985892345.provider.plugin.kcp.ir.body

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
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
 * 2023/6/15 15:48
 */
class SingleImplProviderHandler : ProviderHandler {
  
  private val singleImplProviderAnnotation = FqName("com.g985892345.provider.annotation.SingleImplProvider")
  private val clazzArg = Name.identifier("clazz")
  private val nameArg = Name.identifier("name")
  private lateinit var singleImplProviderFunction: IrSimpleFunction
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
        it.name.asString() == "addSingleImplProvider"
      }
    singleImplProviderFunction = ktProviderInitializerImpl.functions
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
    if (kind.isClass || kind.isObject) {
      descriptor.annotations
        .filter { it.fqName == singleImplProviderAnnotation }
        .forEach {
          if (kind.isClass) {
            descriptor.constructors.find { it.valueParameters.isEmpty() }
              ?: throw IllegalStateException("不存在空构造器 class=${descriptor.classId!!.asFqNameString()}")
          }
          val kClass = it.allValueArguments[clazzArg]?.value as KClassValue.Value.NormalClass?
          val name = it.allValueArguments[nameArg]?.value as String?
          val key = getKey(descriptor, kClass?.classId, name)
          val classIdArgMsg = if (kClass != null) "classId = " + kClass.classId.asFqNameString() + "   " else ""
          val nameArgMsg = if (name != null) "name = $name" else ""
          ProviderHandler.putAndCheckUniqueKey(key) {
            "已包含重复的申明: $classIdArgMsg$nameArgMsg"
          }
          messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "@SingleImplProvider: ${descriptor.classId!!.asFqNameString()} -> $classIdArgMsg$nameArgMsg"
          )
          val classSymbol = pluginContext.referenceClass(descriptor.classId!!)!!
          +irAddSingleImplProvider(pluginContext, key, classSymbol, initImplFunction)
        }
    }
  }
  
  private fun IrBuilderWithScope.irAddSingleImplProvider(
    pluginContext: IrPluginContext,
    key: String,
    classSymbol: IrClassSymbol,
    initImplFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(singleImplProviderFunction).also { call ->
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
                if (classSymbol.owner.isObject) {
                  irGetObject(classSymbol)
                } else {
                  irCall(classSymbol.owner.constructors.single { it.valueParameters.isEmpty() })
                }
              )
            }
          },
          IrStatementOrigin.LAMBDA
        )
      )
    }
  }
  
  private fun getKey(descriptor: ClassDescriptor, classId: ClassId?, name: String?): String {
    return if (classId == null) {
      if (name.isNullOrEmpty()) {
        throw IllegalArgumentException("必须设置 clazz 或者 name!   class=${descriptor.classId!!.asFqNameString()}")
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