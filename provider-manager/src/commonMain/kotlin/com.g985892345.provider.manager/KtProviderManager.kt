package com.g985892345.provider.manager

import com.g985892345.provider.init.KtProviderInitializer
import kotlin.reflect.KClass

/**
 * 服务管理者
 * - 你可以实现自己的服务管理者，该类只是简单实现
 *
 * @author 985892345
 * 2023/6/13 22:05
 */
object KtProviderManager {
  
  fun <T : Any> getImplOrNull(name: String, singleton: Boolean? = null): T? =
    getImplOrNullInternal(null, name, singleton)
  
  fun <T : Any> getImplOrNull(clazz: KClass<T>, singleton: Boolean? = null): T? =
    getImplOrNullInternal(clazz, "", singleton)
  
  fun <T : Any> getImplOrNull(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T? =
    getImplOrNullInternal(clazz, name, singleton)
  
  fun <T : Any> getImplOrThrow(name: String, singleton: Boolean? = null): T =
    getImplOrNull(name, singleton)!!
  
  fun <T : Any> getImplOrThrow(clazz: KClass<T>, singleton: Boolean? = null): T =
    getImplOrNull(clazz, singleton)!!
  
  fun <T : Any> getImplOrThrow(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T =
    getImplOrNull(clazz, name, singleton)!!
  
  fun <T : Any> getKClassOrNull(name: String): KClass<out T>? {
    return KtProviderInitializer.KClassProviderMap[name]?.get()
  }
  
  fun <T : Any> getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!
  
  /**
   * 获取 @NewImplProvider 中 clazz 参数为 [clazz] 的所有实现类
   * @return 返回 () -> T 用于延迟初始化
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getNewImpls(clazz: KClass<T>): List<() -> T> {
    return KtProviderInitializer.NewImplProviderMap[clazz]
      ?.values
      ?.map { { it.newInstance() as T } }
      ?: emptyList()
  }
  
  /**
   * 获取 @SingleImplProvider 中 clazz 参数为 [clazz] 的所有实现类
   * @return 返回 () -> T 用于延迟初始化
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getSingleImpls(clazz: KClass<T>): List<() -> T> {
    return KtProviderInitializer.SingleImplProviderMap[clazz]
      ?.values
      ?.map { { it.getInstance() as T } }
      ?: emptyList()
  }
  
  @Suppress("UNCHECKED_CAST")
  private fun <T> getImplOrNullInternal(
    clazz: KClass<*>?,
    name: String,
    singleton: Boolean?
  ): T? {
    if (clazz == null && name.isEmpty()) {
      throw IllegalArgumentException("必须包含 clazz 或者 name!")
    }
    val clazz2 = clazz ?: Nothing::class
    return when (singleton) {
      null -> {
        KtProviderInitializer.SingleImplProviderMap[clazz2]?.get(name)?.getInstance() as T?
          ?: KtProviderInitializer.NewImplProviderMap[clazz2]?.get(name)?.newInstance() as T?
      }
      true -> KtProviderInitializer.SingleImplProviderMap[clazz2]?.get(name)?.getInstance() as T?
      false -> KtProviderInitializer.NewImplProviderMap[clazz2]?.get(name)?.newInstance() as T?
    }
  }
}