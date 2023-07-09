package com.g985892345.provider.init.wrapper

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 11:41
 */
sealed interface IProviderWrapper {
  fun get(): Any
}

open class NewImplProviderWrapper(
  private val init: () -> Any
) : IProviderWrapper {
  override fun get(): Any {
    return init.invoke()
  }
}

open class SingleImplProviderWrapper(
  init: () -> Any
) : IProviderWrapper {
  // 由官方保证线程安全
  val lazy by lazy(init)
  override fun get(): Any {
    return lazy
  }
}

open class KClassProviderWrapper(
  private val init: () -> Any
) : IProviderWrapper {
  override fun get(): Any {
    return init.invoke()
  }
}

