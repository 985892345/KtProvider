package com.g985892345.provider.manager

import com.g985892345.provider.init.KtProviderInitializer
import kotlin.contracts.CallsInPlace
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
   *
   * ## 注意
   * - name 不能为 空串
   *
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   * @throws IllegalArgumentException name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrNull(name: String, singleton: Boolean? = null): T? =
    getImplOrNullInternal(null, name, singleton)

  /**
   * 返回只设置了对应 [clazz] 的实现类
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   */
  fun <T : Any> getImplOrNull(clazz: KClass<T>, singleton: Boolean? = null): T? =
    getImplOrNullInternal(clazz, "", singleton)

  /**
   * 返回设置了对应 [clazz] 和 [name] 的实现类
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   */
  fun <T : Any> getImplOrNull(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T? =
    getImplOrNullInternal(clazz, name, singleton)

  /**
   * 返回只设置了对应 [name] 的实现类
   * ## 注意
   *    * - name 不能为 空串
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   * @throws NullPointerException 不存在时抛出空指针
   * @throws IllegalArgumentException name 为空串时抛出非法参数错误
   */
  fun <T : Any> getImplOrThrow(name: String, singleton: Boolean? = null): T =
    getImplOrNull(name, singleton)!!

  /**
   * 返回只设置了对应 [clazz] 的实现类
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<T>, singleton: Boolean? = null): T =
    getImplOrNull(clazz, singleton)!!

  /**
   * 返回设置了对应 [clazz] 和 [name] 的实现类
   * @param singleton false 时返回 @NewImplProvider 的实现类；true 时只返回 @SingleImplProvider 的实现类；null 时两者皆可
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getImplOrThrow(clazz: KClass<T>, name: String = "", singleton: Boolean? = null): T =
    getImplOrNull(clazz, name, singleton)!!

  /**
   * 返回 [name] 对应的 KClass
   */
  fun <T : Any> getKClassOrNull(name: String): KClass<out T>? = KtProviderInitializer.KClassProviderMap[name]?.get()

  /**
   * 返回 [name] 对应的 KClass
   * @throws NullPointerException 不存在时抛出空指针
   */
  fun <T : Any> getKClassOrThrow(name: String): KClass<out T> = getKClassOrNull(name)!!
  
  /**
   * 获取 @NewImplProvider 中 clazz 参数为 [clazz] 的所有实现类
   * @return 返回 () -> T 用于延迟初始化，每次调用都是新的实例
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllNewImpl(clazz: KClass<T>): Map<String, () -> T> {
    // todo 目前 Kotlin/Js 不支持类实现 () -> Any 接口，暂时通过转换解决
    return KtProviderInitializer.NewImplProviderMap[clazz]
      ?.mapValues { { it.value.newInstance() as T } }
      ?: emptyMap()
  }
  
  /**
   * 获取 @SingleImplProvider 中 clazz 参数为 [clazz] 的所有实现类
   * @return 返回 () -> T 用于延迟初始化，每次调用都是同一个实例
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getAllSingleImpl(clazz: KClass<T>): Map<String, () -> T> {
    // todo 目前 Kotlin/Js 不支持类实现 () -> Any 接口，暂时通过转换解决
    return KtProviderInitializer.SingleImplProviderMap[clazz]
      ?.mapValues { { it.value.getInstance() as T } }
      ?: emptyMap()
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