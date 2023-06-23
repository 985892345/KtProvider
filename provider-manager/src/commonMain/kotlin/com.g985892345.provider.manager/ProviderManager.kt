package com.g985892345.provider.manager

import com.g985892345.provider.init.ProviderInitialize
import com.g985892345.provider.init.wrapper.IProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 22:05
 */
object ProviderManager {
  fun getWrapperOrNull(name: String): IProviderWrapper? {
    return ProviderInitialize.ProviderMap[name]
  }
}

@Suppress("UNCHECKED_CAST")
fun <T> ProviderManager.getImplOrNullNoInline(singleton: Boolean?, clazz: KClass<*>, name: String = ""): T? {
  // 由于 Kotlin/Js 不支持 KClass.qualifiedName，所以使用 simpleName，要求接口类名不能出现重复
  // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/qualified-name.html
  val wrapper = getWrapperOrNull(if (name.isEmpty()) clazz.simpleName!! else clazz.simpleName + "-" + name)
  val wrapperType = when (singleton) {
    null -> wrapper is NewImplProviderWrapper || wrapper is SingleImplProviderWrapper
    true -> wrapper is SingleImplProviderWrapper
    false -> wrapper is NewImplProviderWrapper
  }
  return if (wrapperType) wrapper!!.get() as T else null
}

inline fun <reified T> ProviderManager.getImplOrNull(name: String = "", singleton: Boolean? = null): T? =
  getImplOrNullNoInline(singleton, T::class, name)
inline fun <reified T> ProviderManager.getImplOrThrow(name: String = "", singleton: Boolean? = null): T =
  getImplOrNullNoInline(singleton, T::class, name)!!

@Suppress("UNCHECKED_CAST")
fun <T : Any> ProviderManager.getKClassOrNull(name: String): KClass<out T>? {
  val wrapper = getWrapperOrNull(name)
  return if (wrapper is KClassProviderWrapper) wrapper.get() as KClass<out T> else null
}

fun <T : Any> ProviderManager.getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!