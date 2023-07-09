package com.g985892345.provider.init

import com.g985892345.provider.init.wrapper.IProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper

/**
 * 初始化服务
 *
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
interface KtProviderInitializer {
  /**
   * 初始化方法，ir 插桩的地方
   * - 允许重写并添加自己的逻辑
   */
  fun initKtProvider() {
  }
  
  /**
   * 添加一个 NewImplProvider，key 的规则请遵循 KtProviderManager 获取服务时的设置
   */
  fun addNewImplProvider(key: String, init: () -> Any) {
    ProviderMapInternal[key] = NewImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 SingleImplProvider，key 的规则请遵循 KtProviderManager 获取服务时的设置
   */
  fun addSingleImplProvider(key: String, init: () -> Any) {
    ProviderMapInternal[key] = SingleImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 KClassProvider，key 的规则请遵循 KtProviderManager 获取服务时的设置
   */
  fun addKClassProvider(key: String, init: () -> Any) {
    ProviderMapInternal[key] = KClassProviderWrapper(init)
  }
  
  companion object {
    protected val ProviderMapInternal = hashMapOf<String, IProviderWrapper>()
    
    /**
     * key 与 IProviderWrapper，key 为 class 和 name 的结合，具体实现可查看 KtProviderManager 获取服务时的设置
     */
    val ProviderMap: Map<String, IProviderWrapper>
      get() = ProviderMapInternal
  }
}