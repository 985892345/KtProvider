package com.g985892345.provider.manager

import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * Service provider manager
 *
 * @author 985892345
 * 2023/6/13 22:05
 */
object KtProviderManager {

  /**
   * Return the implementation class that is only set with the corresponding [name].
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> getImplOrNull(name: String): T? = getImplOrNull(null, name)
  
  /**
   * Return the implementation class that is only set with the corresponding [name].
   * @throws NullPointerException when it does not exist.
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> getImplOrThrow(name: String): T = getImplOrNull(name)!!
  
  /**
   * Return the implementation class that is only set with the corresponding [clazz].
   */
  fun <T : Any> getImplOrNull(clazz: KClass<out T>): T? = getImplOrNull(clazz, "")
  
  /**
   * Return the implementation class that is only set with the corresponding [clazz].
   * @throws NullPointerException when it does not exist.
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<out T>): T = getImplOrNull(clazz)!!

  /**
   * Return the implementation class that is set with the corresponding [clazz] and [name].
   * @throws IllegalArgumentException when [clazz] is null and [name] is an empty string.
   */
  fun <T : Any> getImplOrNull(clazz: KClass<out T>?, name: String): T? = getImplOrNullInternal(clazz, name)

  /**
   * Return the implementation class that is set with the corresponding [clazz] and [name].
   * @throws NullPointerException when it does not exist.
   * @throws IllegalArgumentException when [clazz] is null and [name] is an empty string.
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<out T>?, name: String): T = getImplOrNull(clazz, name)!!
  
  /**
   * Retrieve all implementation classes from @ImplProvider annotations where the clazz parameter is [clazz].
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllImpl(clazz: KClass<out T>?): Map<String, ImplProviderWrapper<T>> {
    return IKtProviderDelegate.ImplProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as ImplProviderWrapper<T> }
      ?: emptyMap()
  }
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [name].
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> getKClassOrNull(name: String): KClass<out T>? = getKClassOrNull(null, name)
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [name].
   * @throws NullPointerException when it does not exist.
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [clazz].
   */
  fun <T : Any> getKClassOrNull(clazz: KClass<out T>): KClass<out T>? = getKClassOrNull(clazz, "")
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [clazz].
   * @throws NullPointerException when it does not exist.
   */
  fun <T : Any> getKClassOrThrow(clazz: KClass<out T>): KClass<out T>? = getKClassOrNull(clazz)!!
  
  /**
   * 返回 [clazz] 和 [name] 对应的 KClass
   * @throws IllegalArgumentException class 为 null 并且 name 为空串时抛出非法参数错误
   */
  fun <T : Any> getKClassOrNull(clazz: KClass<out T>?, name: String): KClass<out T>? = getKClassOrNullInternal(clazz, name)
  
  /**
   * Return the KClass of the implementation class that is set with the corresponding [clazz] and [name].
   * @throws NullPointerException when it does not exist.
   */
  fun <T : Any> getKClassOrThrow(clazz: KClass<out T>?, name: String): KClass<out T> = getKClassOrNull(clazz, name)!!
  
  /**
   * Retrieve all implementation classes from @KClassProvider annotations where the clazz parameter is [clazz].
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllKClass(clazz: KClass<out T>?): Map<String, KClassProviderWrapper<T>> {
    return IKtProviderDelegate.KClassProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as KClassProviderWrapper<T> }
      ?: emptyMap()
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T> getImplOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): T? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("Either [clazz] or [name] must be included!")
    }
    return IKtProviderDelegate.ImplProviderMap[clazz]?.get(name)?.get() as T?
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> getKClassOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): KClass<out T>? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("Either [clazz] or [name] must be included!")
    }
    return IKtProviderDelegate.KClassProviderMap[clazz]?.get(name)?.get() as KClass<out T>?
  }
}