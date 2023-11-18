package com.g985892345.provider.manager

import com.g985892345.provider.init.KtProviderInitializer
import com.g985892345.provider.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
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
   * 返回只设置了对应 [name] 的实现类
   * @throws IllegalArgumentException name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrNull(name: String): T? = getImplOrNull(null, name)

  /**
   * 返回只设置了对应 [clazz] 的实现类
   */
  fun <T : Any> getImplOrNull(clazz: KClass<out T>): T? = getImplOrNull(clazz, "")

  /**
   * 返回设置了对应 [clazz] 和 [name] 的实现类
   * @throws IllegalArgumentException class 为 null 并且 name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrNull(clazz: KClass<out T>?, name: String): T? = getImplOrNullInternal(clazz, name)

  /**
   * 返回只设置了对应 [name] 的实现类
   * @throws NullPointerException 不存在时抛出空指针
   * @throws IllegalArgumentException name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrThrow(name: String): T = getImplOrNull(name)!!

  /**
   * 返回只设置了对应 [clazz] 的实现类
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<out T>): T = getImplOrNull(clazz)!!

  /**
   * 返回设置了对应 [clazz] 和 [name] 的实现类
   * @throws NullPointerException 不存在时抛出空指针
   * @throws IllegalArgumentException class 为 null 并且 name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<out T>?, name: String): T = getImplOrNull(clazz, name)!!
  
  /**
   * 获取 @ImplProvider 中 clazz 参数为 [clazz] 的所有实现类
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllImpl(clazz: KClass<out T>?): Map<String, ImplProviderWrapper<T>> {
    return KtProviderInitializer.ImplProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as ImplProviderWrapper<T> }
      ?: emptyMap()
  }
  
  /**
   * 返回 [name] 对应的 KClass
   * @throws IllegalArgumentException name 为空串时抛出非法参数错误
   */
  fun <T : Any> getKClassOrNull(name: String): KClass<out T>? = getKClassOrNull(null, name)
  
  /**
   * 返回只设置了对应 [clazz] 的实现类
   */
  fun <T : Any> getKClassOrNull(clazz: KClass<out T>): KClass<out T>? = getKClassOrNull(clazz, "")

  /**
   * 返回 [clazz] 和 [name] 对应的 KClass
   * @throws IllegalArgumentException class 为 null 并且 name 为空串时抛出非法参数错误
   */
  fun <T : Any> getKClassOrNull(clazz: KClass<out T>?, name: String): KClass<out T>? = getKClassOrNullInternal(clazz, name)
  
  /**
   * 返回 [name] 对应的 KClass
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!
  
  /**
   * 返回只设置了对应 [clazz] 的实现类
   */
  fun <T : Any> getKClassOrThrow(clazz: KClass<out T>): KClass<out T>? = getKClassOrNull(clazz)!!
  
  /**
   * 返回 [clazz] 和 [name] 对应的 KClass
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getKClassOrThrow(clazz: KClass<out T>?, name: String): KClass<out T> = getKClassOrNull(clazz, name)!!
  
  /**
   * 获取 @KClassProvider 中 clazz 参数为 [clazz] 的所有实现类
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllKClass(clazz: KClass<out T>?): Map<String, KClassProviderWrapper<T>> {
    return KtProviderInitializer.KClassProviderMap[clazz ?: Nothing::class]
      ?.mapValues { it.value as KClassProviderWrapper<T> }
      ?: emptyMap()
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T> getImplOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): T? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("必须包含 clazz 或者 name!")
    }
    val clazz2 = clazz ?: Nothing::class
    return KtProviderInitializer.ImplProviderMap[clazz2]?.get(name)?.get() as T?
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> getKClassOrNullInternal(
    clazz: KClass<*>?,
    name: String,
  ): KClass<out T>? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("必须包含 clazz 或者 name!")
    }
    val clazz2 = clazz ?: Nothing::class
    return KtProviderInitializer.KClassProviderMap[clazz2]?.get(name)?.get() as KClass<out T>?
  }
}