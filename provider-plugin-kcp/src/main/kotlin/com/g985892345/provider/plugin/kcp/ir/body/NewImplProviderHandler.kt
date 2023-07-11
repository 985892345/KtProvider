package com.g985892345.provider.plugin.kcp.ir.body

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.isClass
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.starProjectedType
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
class NewImplProviderHandler(val isCheckImpl: Boolean) : ProviderHandler {
  
  private val newImplProviderAnnotation = FqName("com.g985892345.provider.annotation.NewImplProvider")
  private val clazzArg = Name.identifier("clazz")
  private val nameArg = Name.identifier("name")
  private lateinit var newImplProviderFunction: IrSimpleFunction
  private lateinit var messageCollector: MessageCollector
  private lateinit var nothingSymbol: IrClassSymbol
  
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
        it.name.asString() == "addNewImplProvider"
      }
    newImplProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
    nothingSymbol =
      pluginContext.referenceClass(ClassId(FqName("kotlin"), FqName("Nothing"), false))!!
  }
  
  override fun IrBlockBodyBuilder.processInitImplFunction(
    pluginContext: IrPluginContext,
    initImplFunction: IrSimpleFunction,
    descriptor: ClassDescriptor
  ) {
    val kind = descriptor.kind
    if (kind.isClass && (descriptor.modality == Modality.OPEN || descriptor.modality == Modality.FINAL)) {
      descriptor.annotations
        .filter { it.fqName == newImplProviderAnnotation }
        .forEach { annotation ->
          descriptor.constructors.find { it.valueParameters.isEmpty() }
            ?: throw IllegalStateException("不存在空构造器 class=${descriptor.classId!!.asFqNameString()}")
          val kClass = annotation.allValueArguments[clazzArg]?.value as KClassValue.Value.NormalClass?
          val name = annotation.allValueArguments[nameArg]?.value as String?
          if (kClass == null && name == null) {
            throw IllegalArgumentException("必须设置 clazz 或者 name!   class=${descriptor.classId!!.asFqNameString()}")
          }
          val classIdArgMsg = if (kClass != null) "clazz=" + kClass.classId.asFqNameString() + "   " else ""
          val nameArgMsg = if (name != null) "name=$name" else ""
          ProviderHandler.putAndCheckUniqueKey(kClass?.classId?.asFqNameString() + " " + name) {
            "已包含重复的申明: $classIdArgMsg$nameArgMsg   class=${descriptor.classId!!.asFqNameString()}"
          }
          val implSymbol = pluginContext.referenceClass(descriptor.classId!!)!!
          val clazzArgSymbol = kClass?.classId?.let { pluginContext.referenceClass(it) }
          if (isCheckImpl && clazzArgSymbol != null && !implSymbol.isSubtypeOfClass(clazzArgSymbol)) {
            throw IllegalStateException("被注解类不是注解中标注参数的实现类   class=${descriptor.classId!!.asFqNameString()}")
          }
          messageCollector.report(
            CompilerMessageSeverity.INFO,
            "@NewImplProvider: ${descriptor.classId!!.asFqNameString()} -> $classIdArgMsg$nameArgMsg"
          )
          val irConstructor = implSymbol.owner
            .constructors
            .find { it.valueParameters.isEmpty() }!!
          +irAddNewImplProvider(
            pluginContext,
            clazzArgSymbol,
            name,
            irConstructor,
            initImplFunction
          )
        }
    }
  }
  
  private fun IrBuilderWithScope.irAddNewImplProvider(
    pluginContext: IrPluginContext,
    clazzArg: IrClassSymbol?,
    nameArg: String?,
    emptyConstructor: IrConstructor,
    initImplFunction: IrSimpleFunction,
  ): IrExpression {
    return irCall(newImplProviderFunction).also { call ->
      call.dispatchReceiver = irGet(initImplFunction.dispatchReceiverParameter!!)
      call.putValueArgument(
        0,
        IrClassReferenceImpl(
          startOffset, endOffset,
          context.irBuiltIns.kClassClass.starProjectedType,
          context.irBuiltIns.kClassClass,
          (clazzArg ?: nothingSymbol).defaultType
        )
      )
      call.putValueArgument(1, irString(nameArg ?: ""))
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