package com.g985892345.provider.manager

import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * Service provider manager
 *
 * @author 985892345
 * 2024/12/19 23:17
 */
object KtProvider {
  
  /**
   * Return the implementation class that is only set with the corresponding [name].
   * @throws NullPointerException when it does not exist.
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> impl(name: String): T = implOrNull(name)!!
  
  /**
   * Return the implementation class that is only set with the corresponding [name].
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> implOrNull(name: String): T? = implOrNullInternal(null, name)
  
  /**
   * Return the implementation class that is only set with the corresponding [clazz].
   * @throws NullPointerException when it does not exist.
   */
  fun <T : Any> impl(clazz: KClass<out T>, name: String = ""): T = implOrNull(clazz, name)!!
  
  /**
   * Return the implementation class that is only set with the corresponding [clazz].
   */
  fun <T : Any> implOrNull(clazz: KClass<out T>, name: String = ""): T? = implOrNullInternal(clazz, name)
  
  /**
   * Retrieve all implementation classes from @ImplProvider annotations where the clazz parameter is [clazz].
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> allImpl(clazz: KClass<out T>?): Map<String, ImplProviderWrapper<T>> {
    return IKtProviderDelegate.ImplProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as ImplProviderWrapper<T> }
      ?: emptyMap()
  }
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [name].
   * @throws NullPointerException when it does not exist.
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> clazz(name: String): KClass<out T> = clazzOrNull(name)!!
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [name].
   * @throws IllegalArgumentException when [name] is an empty string.
   */
  fun <T : Any> clazzOrNull(name: String): KClass<out T>? = clazzOrNullInternal(null, name)
  
  /**
   * Return the KClass of the implementation class that is only set with the corresponding [clazz].
   * @throws NullPointerException when it does not exist.
   */
  fun <T : Any> clazz(clazz: KClass<out T>, name: String = ""): KClass<out T> = clazzOrNull(clazz, name)!!
  
  /**
   * Return the KClass of the implementation class that is set with the corresponding [clazz] and [name].
   * @throws IllegalArgumentException when [clazz] is null and [name] is an empty string.
   */
  fun <T : Any> clazzOrNull(clazz: KClass<out T>, name: String = ""): KClass<out T>? = clazzOrNullInternal(clazz, name)
  
  /**
   * Retrieve all implementation classes from @KClassProvider annotations where the clazz parameter is [clazz].
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> allClazz(clazz: KClass<out T>?): Map<String, KClassProviderWrapper<T>> {
    return IKtProviderDelegate.KClassProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as KClassProviderWrapper<T> }
      ?: emptyMap()
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T> implOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): T? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("Either [clazz] or [name] must be included!")
    }
    return IKtProviderDelegate.ImplProviderMap[clazz]?.get(name)?.get() as T?
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> clazzOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): KClass<out T>? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("Either [clazz] or [name] must be included!")
    }
    return IKtProviderDelegate.KClassProviderMap[clazz]?.get(name)?.get() as KClass<out T>?
  }
}