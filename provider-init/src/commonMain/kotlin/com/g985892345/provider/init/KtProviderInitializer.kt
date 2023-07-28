package com.g985892345.provider.init

import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper
import kotlin.reflect.KClass

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
   * 添加一个 @NewImplProvider 的初始化 init，可重写自定义逻辑
   */
  fun addNewImplProvider(clazz: KClass<*>, name: String, init: () -> Any) {
    NewImplProviderMapInternal.getOrPut(clazz) { hashMapOf() }[name] = NewImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 @SingleImplProvider 的初始化 init，可重写自定义逻辑
   */
  fun addSingleImplProvider(clazz: KClass<*>, name: String, init: () -> Any) {
    SingleImplProviderMapInternal.getOrPut(clazz) { hashMapOf() }[name] = SingleImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 @KClassProvider 的初始化 init，可重写自定义逻辑
   */
  fun addKClassProvider(name: String, init: () -> KClass<*>) {
    KClassProviderMapInternal[name] = KClassProviderWrapper(init)
  }
  
  companion object {
    // 你不应该对这几个 Map 进行直接修改，而是通过重写 add* 方法来实现自定义
    private val NewImplProviderMapInternal = hashMapOf<KClass<*>, HashMap<String, NewImplProviderWrapper>>()
    private val SingleImplProviderMapInternal = hashMapOf<KClass<*>, HashMap<String, SingleImplProviderWrapper>>()
    private val KClassProviderMapInternal = hashMapOf<String, KClassProviderWrapper>()
    
    val NewImplProviderMap: Map<KClass<*>, Map<String, NewImplProviderWrapper>>
      get() = NewImplProviderMapInternal
    val SingleImplProviderMap: Map<KClass<*>, Map<String, SingleImplProviderWrapper>>
      get() = SingleImplProviderMapInternal
    val KClassProviderMap: Map<String, KClassProviderWrapper>
      get() = KClassProviderMapInternal
  }
}