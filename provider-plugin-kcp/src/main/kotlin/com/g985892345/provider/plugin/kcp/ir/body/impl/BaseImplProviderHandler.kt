package com.g985892345.provider.plugin.kcp.ir.body.impl

import com.g985892345.provider.plugin.kcp.ir.body.ProviderHandler
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * .
 *
 * @author 985892345
 * 2023/7/21 22:38
 */
abstract class BaseImplProviderHandler(
  val isCheckImpl: Boolean
) : ProviderHandler {
  
  protected val clazzArg = Name.identifier("clazz")
  protected val nameArg = Name.identifier("name")
  protected lateinit var messageCollector: MessageCollector
  protected lateinit var nothingSymbol: IrClassSymbol
  
  override fun init(
    pluginContext: IrPluginContext,
    moduleFragment: IrModuleFragment,
    messageCollector: MessageCollector
  ) {
    this.messageCollector = messageCollector
    nothingSymbol =
      pluginContext.referenceClass(ClassId(FqName("kotlin"), FqName("Nothing"), false))!!
  }
  
  // 检查 class 是否存在空构造器
  protected fun checkEmptyConstructor(irClass: IrClass) {
    irClass.constructors.find { it.valueParameters.isEmpty() }
      ?: throw IllegalStateException("不存在空构造器   位置：${irClass.location}")
  }
  
  // 检查被注解类是否是 clazz 参数的实现类
  protected fun checkImpl(irClass: IrClass, classReference: IrClassReference?) {
    if (!isCheckImpl) return
    if (classReference == null) return
    // 这里是获取注解中 clazz 参数表示的 IrClass 对象，不应该会出现空
    val classReferenceIrClass = classReference.classType.getClass()!!
    if (!irClass.isSubclassOf(classReferenceIrClass)) {
      throw IllegalStateException("被注解类不是注解中标注参数的实现类   位置：${irClass.location}")
    }
  }
  
  // 获取注解参数
  @Suppress("UNCHECKED_CAST")
  protected fun getImplProviderArg(
    irClass: IrClass,
    annotation: IrConstructorCall
  ): ImplProviderArg {
    val location = irClass.location
    // 获取 clazz 参数
    val clazz = annotation.getValueArgument(clazzArg) as IrClassReference?
    // 获取 name 参数
    val name = (annotation.getValueArgument(nameArg) as IrConst<String>?)?.value
    // 检查参数合法性
    if (clazz == null) {
      if (name == null) {
        throw IllegalArgumentException("必须设置 clazz 或者 name!   位置：$location")
      } else if (name.isEmpty()) {
        throw IllegalArgumentException("在不设置 clazz 时 name 不能为空串!   位置：$location")
      }
    }
    val arg = ImplProviderArg(clazz, name)
    // 检查是否重复
    putAndCheckUniqueImplKey(arg.msg, location)
    return arg
  }
  
  protected val IrClass.location: String
    get() = classId!!.asFqNameString()
  
  protected data class ImplProviderArg(
    val classReference: IrClassReference?,
    val name: String?
  ) {
    // 打印信息
    val msg = when {
      classReference == null && name == null -> ""
      classReference == null && name != null -> "name=$name"
      classReference != null && name == null -> "clazz=${classReference.classType.classFqName}"
      classReference != null && name != null -> "clazz=${classReference.classType.classFqName}, name=$name"
      else -> error("")
    }
  }
  
  companion object {
    private val UniqueKey = hashMapOf<String, String>()
    
    fun putAndCheckUniqueImplKey(key: String, locationMsg: String) {
      val lastLocationMsg = UniqueKey[key]
      if (lastLocationMsg != null) {
        throw IllegalArgumentException("包含重复的声明: $key\n位置1: $lastLocationMsg\n位置2: $locationMsg")
      } else {
        UniqueKey[key] = locationMsg
      }
    }
  }
}