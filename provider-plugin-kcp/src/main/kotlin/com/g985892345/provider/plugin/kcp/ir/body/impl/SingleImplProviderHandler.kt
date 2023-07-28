package com.g985892345.provider.plugin.kcp.ir.body.impl

import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
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
  isCheckImpl: Boolean
) : BaseImplProviderHandler(
  "addSingleImplProvider",
  isCheckImpl
) {
  
  private val singleImplProviderAnnotation = FqName("com.g985892345.provider.annotation.SingleImplProvider")
  
  override fun IrBlockBodyBuilder.processInitImplFunction(
    pluginContext: IrPluginContext,
    initImplFunction: IrSimpleFunction,
    descriptor: ClassDescriptor
  ) {
    val kind = descriptor.kind
    val modality = descriptor.modality
    // 筛选抽象 class 和 object
    if (kind.isClass && (modality == Modality.OPEN || modality == Modality.FINAL) || kind.isObject) {
      // 遍历所有注解
      descriptor.annotations
        .filter { it.fqName == singleImplProviderAnnotation }
        .forEach { annotation ->
          checkEmptyConstructor(descriptor)
          val arg = getImplProviderArg(pluginContext, descriptor, annotation)
          val implSymbol = pluginContext.referenceClass(descriptor.classId!!)!!
          checkImpl(implSymbol, arg.clazzSymbol)
          messageCollector.log("@SingleImplProvider: ${descriptor.location} -> (${arg.msg})")
          +irAddSingleImplProvider(pluginContext, arg, implSymbol, initImplFunction)
        }
    }
  }
  
  private fun IrBuilderWithScope.irAddSingleImplProvider(
    pluginContext: IrPluginContext,
    arg: ImplProviderArg,
    classSymbol: IrClassSymbol,
    initImplFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(implProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      // 添加 KClass 参数
      call.putValueArgument(
        0,
        IrClassReferenceImpl(
          startOffset, endOffset,
          context.irBuiltIns.kClassClass.starProjectedType,
          context.irBuiltIns.kClassClass,
          (arg.clazzSymbol ?: nothingSymbol).defaultType // clazzSymbol 为 null 时默认填充 Nothing::class，因为 Nothing 无实现类
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
}