package com.g985892345.provider.plugin.kcp.ir.body.impl

import com.g985892345.provider.plugin.kcp.ir.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.isClass
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
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
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 14:57
 */
class NewImplProviderHandler(
  isCheckImpl: Boolean
) : BaseImplProviderHandler(isCheckImpl) {
  
  private val newImplProviderAnnotation = FqName("com.g985892345.provider.annotation.NewImplProvider")
  private val mIrClassWithAnnotation = mutableListOf<Pair<IrClass, IrConstructorCall>>()
  
  override fun selectIrClass(pluginContext: IrPluginContext, moduleFragment: IrModuleFragment, irClass: IrClass) {
    val kind = irClass.kind
    val modality = irClass.modality
    // 普通 class
    if (kind.isClass && (modality == Modality.OPEN || modality == Modality.FINAL)) {
      irClass.annotations.forEach {
        if (it.isAnnotation(newImplProviderAnnotation)) {
          mIrClassWithAnnotation.add(irClass to it)
        }
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
        it.name.asString() == "addNewImplProvider"
      }
    val implProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
    mIrClassWithAnnotation.forEach { pair ->
      val irClass = pair.first
      val annotation = pair.second
      checkEmptyConstructor(irClass)
      val arg = getImplProviderArg(irClass, annotation)
      checkImpl(irClass, arg.classReference)
      messageCollector.log("@NewImplProvider: ${irClass.location} -> (${arg.msg})")
      val irConstructor = irClass.constructors
        .single { it.valueParameters.isEmpty() }
      +irAddNewImplProvider(pluginContext, arg, irConstructor, initImplFunction, implProviderFunction)
    }
  }
  
  private fun IrBuilderWithScope.irAddNewImplProvider(
    pluginContext: IrPluginContext,
    arg: ImplProviderArg,
    emptyConstructor: IrConstructor,
    initImplFunction: IrSimpleFunction,
    implProviderFunction: IrSimpleFunction
  ): IrExpression {
    return irCall(implProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      // 添加 KClass 参数
      call.putValueArgument(
        0,
        arg.classReference ?: IrClassReferenceImpl(
          startOffset, endOffset,
          context.irBuiltIns.kClassClass.starProjectedType,
          context.irBuiltIns.kClassClass,
          nothingSymbol.defaultType // clazzSymbol 为 null 时默认填充 Nothing::class，因为 Nothing 无实现类
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
                irCall(emptyConstructor)
              )
            }
          },
          IrStatementOrigin.LAMBDA
        )
      )
    }
  }
}