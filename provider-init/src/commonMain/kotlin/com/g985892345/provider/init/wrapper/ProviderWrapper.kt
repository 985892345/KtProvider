package com.g985892345.provider.init.wrapper

import kotlin.reflect.KClass

/**
 * 包裹初始化 init 的 Wrapper
 *
 * @author 985892345
 * 2023/6/14 11:41
 */
sealed interface ProviderWrapper<T : Any, E : Any> {
  val name: String
  val clazz: KClass<T>
  fun get(): E
}

class ImplProviderWrapper<T: Any>(
  override val name: String,
  override val clazz: KClass<T>,
  private val init: () -> T
) : ProviderWrapper<T, T> {
  override fun get(): T = init.invoke()
}

class KClassProviderWrapper<T: Any>(
  override val name: String,
  override val clazz: KClass<T>,
  private val init: () -> KClass<out T>
) : ProviderWrapper<T, KClass<out T>> {
  override fun get(): KClass<out T> = init.invoke()
}

