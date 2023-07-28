package com.g985892345.provider.plugin.kcp.ir.body.impl

import com.g985892345.provider.plugin.kcp.ir.body.ProviderHandler
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.classId

/**
 * .
 *
 * @author 985892345
 * 2023/7/21 22:38
 */
abstract class BaseImplProviderHandler(
  val addProviderFunctionName: String,
  val isCheckImpl: Boolean
) : ProviderHandler {
  
  protected val clazzArg = Name.identifier("clazz")
  protected val nameArg = Name.identifier("name")
  protected lateinit var implProviderFunction: IrSimpleFunction
  protected lateinit var messageCollector: MessageCollector
  protected lateinit var nothingSymbol: IrClassSymbol
  
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
        it.name.asString() == addProviderFunctionName
      }
    implProviderFunction = ktProviderInitializerImpl.functions
      .single {
        it.overrides(superClassFunction)
      }
    nothingSymbol =
      pluginContext.referenceClass(ClassId(FqName("kotlin"), FqName("Nothing"), false))!!
  }
  
  // 检查 class 是否存在空构造器
  protected fun checkEmptyConstructor(descriptor: ClassDescriptor) {
    descriptor.constructors.find { it.valueParameters.isEmpty() }
      ?: throw IllegalStateException("不存在空构造器   位置：${descriptor.location}")
  }
  
  // 检查被注解类是否是 clazz 参数的实现类
  protected fun checkImpl(implSymbol: IrClassSymbol, clazzSymbol: IrClassSymbol?) {
    if (isCheckImpl && clazzSymbol != null && !implSymbol.isSubtypeOfClass(clazzSymbol)) {
      throw IllegalStateException("被注解类不是注解中标注参数的实现类   位置：${implSymbol.location}")
    }
  }
  
  // 获取注解参数
  protected fun getImplProviderArg(
    pluginContext: IrPluginContext,
    descriptor: ClassDescriptor,
    annotation: AnnotationDescriptor
  ): ImplProviderArg {
    val location = descriptor.location
    // 获取 clazz 参数
    val clazz = annotation.allValueArguments[clazzArg]?.value as KClassValue.Value.NormalClass?
    // 获取 name 参数
    val name = annotation.allValueArguments[nameArg]?.value as String?
    // 检查参数合法性
    if (clazz == null) {
      if (name == null) {
        throw IllegalArgumentException("必须设置 clazz 或者 name!   位置：$location")
      } else if (name.isEmpty()) {
        throw IllegalArgumentException("在不设置 clazz 时 name 不能为空串!   位置：$location")
      }
    }
    val arg = ImplProviderArg(pluginContext, clazz, name)
    // 检查是否重复
    putAndCheckUniqueImplKey(arg.msg, location)
    return arg
  }
  
  protected val ClassDescriptor.location: String
    get() = classId!!.asFqNameString()
  
  protected val IrClassSymbol.location: String
    get() = owner.classId!!.asFqNameString()
  
  protected data class ImplProviderArg(
    val pluginContext: IrPluginContext,
    val clazz: KClassValue.Value.NormalClass?,
    val name: String?
  ) {
    val clazzId: ClassId? = clazz?.classId
    val clazzSymbol: IrClassSymbol? = clazzId?.let { pluginContext.referenceClass(it) }
    
    // 打印信息
    val msg = when {
      clazzSymbol == null && name == null -> ""
      clazzSymbol == null && name != null -> "name=$name"
      clazzSymbol != null && name == null -> "clazz=${clazzSymbol.owner.classId?.asFqNameString()}"
      clazzSymbol != null && name != null -> "clazz=${clazzSymbol.owner.classId?.asFqNameString()}, name=$name"
      else -> error("")
    }
  }
  
  companion object {
    private val UniqueKey = hashMapOf<String, String>()
    
    fun putAndCheckUniqueImplKey(key: String, locationMsg: String) {
      val lastLocationMsg = UniqueKey[key]
      if (lastLocationMsg != null) {
        throw IllegalArgumentException("包含重复的申明: $key\n位置1: $lastLocationMsg\n位置2: $locationMsg")
      } else {
        UniqueKey[key] = locationMsg
      }
    }
  }
}