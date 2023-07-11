package com.g985892345.provider.init.wrapper

import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 11:41
 */
open class NewImplProviderWrapper(protected val init: () -> Any) {
  open fun newInstance(): Any {
    return init.invoke()
  }
}

open class SingleImplProviderWrapper(protected val init: () -> Any) {
  // 由官方保证线程安全
  private val value by lazy(LazyThreadSafetyMode.SYNCHRONIZED, init)
  open fun getInstance(): Any {
    return value
  }
}

open class KClassProviderWrapper(protected val init: () -> KClass<*>) {
  open fun <T : Any> get(): KClass<out T> {
    @Suppress("UNCHECKED_CAST")
    return init.invoke() as KClass<out T>
  }
}

