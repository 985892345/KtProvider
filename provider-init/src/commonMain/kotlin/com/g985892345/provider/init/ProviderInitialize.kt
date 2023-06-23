package com.g985892345.provider.init

import com.g985892345.provider.init.wrapper.IProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
abstract class ProviderInitialize {
  open fun init() {
  }
  
  protected open fun addNewImplProvider(name: String, init: () -> Any) {
    ProviderMapInternal[name] = NewImplProviderWrapper(init)
  }
  
  protected open fun addSingleImplProvider(name: String, init: () -> Any) {
    ProviderMapInternal[name] = SingleImplProviderWrapper(init)
  }
  
  protected open fun addKClassProvider(name: String, init: () -> Any) {
    ProviderMapInternal[name] = KClassProviderWrapper(init)
  }
  
  companion object {
    protected val ProviderMapInternal = hashMapOf<String, IProviderWrapper>()
    val ProviderMap: Map<String, IProviderWrapper>
      get() = ProviderMapInternal
  }
}