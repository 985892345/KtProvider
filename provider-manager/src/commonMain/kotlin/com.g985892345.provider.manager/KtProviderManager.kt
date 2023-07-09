package com.g985892345.provider.manager

import com.g985892345.provider.init.KtProviderInitializer
import com.g985892345.provider.init.wrapper.IProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper
import kotlin.reflect.KClass

/**
 * 服务管理者
 * - 你可以实现自己的服务管理者，该类只是简单实现
 *
 * @author 985892345
 * 2023/6/13 22:05
 */
object KtProviderManager {
  /**
   * 获取 key 对应的 [IProviderWrapper]
   *
   * 如果你想实现自己的服务提供者，可以参考该实现
   */
  internal fun getWrapperOrNull(key: String): IProviderWrapper? {
    return KtProviderInitializer.ProviderMap[key]
  }
}

@Suppress("UNCHECKED_CAST")
private fun <T> KtProviderManager.getImplOrNullInternal(
  clazz: KClass<*>?,
  name: String,
  singleton: Boolean?
): T? {
  val key = if (clazz == null) {
    name.ifEmpty { throw IllegalArgumentException("必须包含 clazz 或者 name!") }
  } else {
    // 由于 Kotlin/Js 不支持 KClass.qualifiedName，所以使用 simpleName，要求接口类名不能出现重复
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/qualified-name.html
    if (name.isEmpty()) clazz.simpleName!! else clazz.simpleName + "-" + name
  }
  val wrapper = getWrapperOrNull(key)
  val wrapperType = when (singleton) {
    null -> wrapper is NewImplProviderWrapper || wrapper is SingleImplProviderWrapper
    true -> wrapper is SingleImplProviderWrapper
    false -> wrapper is NewImplProviderWrapper
  }
  return if (wrapperType) wrapper!!.get() as T else null
}

fun <T : Any> KtProviderManager.getImplOrNull(name: String, singleton: Boolean? = null): T? =
  getImplOrNullInternal(null, name, singleton)
fun <T : Any> KtProviderManager.getImplOrNull(clazz: KClass<T>, singleton: Boolean? = null): T? =
  getImplOrNullInternal(clazz, "", singleton)
fun <T : Any> KtProviderManager.getImplOrNull(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T? =
  getImplOrNullInternal(clazz, name, singleton)

fun <T : Any> KtProviderManager.getImplOrThrow(name: String, singleton: Boolean? = null): T =
  getImplOrNull(name, singleton)!!
fun <T : Any> KtProviderManager.getImplOrThrow(clazz: KClass<T>, singleton: Boolean? = null): T =
  getImplOrNull(clazz, singleton)!!
fun <T : Any> KtProviderManager.getImplOrThrow(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T =
  getImplOrNull(clazz, name, singleton)!!

@Suppress("UNCHECKED_CAST")
fun <T : Any> KtProviderManager.getKClassOrNull(name: String): KClass<out T>? {
  val wrapper = getWrapperOrNull(name)
  return if (wrapper is KClassProviderWrapper) wrapper.get() as KClass<out T> else null
}

fun <T : Any> KtProviderManager.getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!