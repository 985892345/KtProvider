package com.g985892345.provider.init.wrapper

import kotlin.reflect.KClass

/**
 * 包裹初始化 init 的 Wrapper
 *
 * @author 985892345
 * 2023/6/14 11:41
 */
class NewImplProviderWrapper(private val init: () -> Any) {
  fun newInstance(): Any {
    return init.invoke()
  }
}

class SingleImplProviderWrapper(init: () -> Any) {
  // 由官方保证线程安全
  private val value by lazy(LazyThreadSafetyMode.SYNCHRONIZED, init)
  fun getInstance(): Any {
    return value
  }
}

class KClassProviderWrapper(val init: () -> KClass<*>) {
  fun <T : Any> get(): KClass<out T> {
    @Suppress("UNCHECKED_CAST")
    return init.invoke() as KClass<out T>
  }
}

