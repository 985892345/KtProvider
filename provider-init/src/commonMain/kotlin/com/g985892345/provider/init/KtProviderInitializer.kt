package com.g985892345.provider.init

import com.g985892345.provider.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * 初始化服务
 *
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
abstract class KtProviderInitializer {
  
  private var mHasInit = false
  
  /**
   * 防止重复加载
   */
  open fun tryInitKtProvider() {
    if (mHasInit) return
    mHasInit = true
    initKtProvider()
  }
  
  protected open fun initKtProvider() {
    initAddAllProvider()
  }
  
  /**
   * 这里会使用 ir 在实现类中插入调用 _initImpl() 方法的逻辑
   * _initImpl() 方法体包含所有的 [addImplProvider] 和 [addKClassProvider]
   * 如果你想拦截某个 Provider 添加，则可以在 ktProvider 闭包中设置代理类
   */
  protected open fun initAddAllProvider() {
  }
  
  /**
   * 添加一个 @ImplProvider 的初始化 init，由 ir 调用
   */
  open fun <T : Any> addImplProvider(clazz: KClass<T>, name: String, init: () -> T) {
    val wrapper = ImplProviderWrapper(name, clazz, init)
    val oldWrapper = ImplProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
    if (oldWrapper != null) {
      val clazzInfo = if (clazz != Nothing::class) "，clazz=${clazz}" else ""
      val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
      throw IllegalStateException("@ImplProvider 注解出现重复$clazzInfo$nameInfo")
    }
  }
  
  /**
   * 添加一个 @KClassProvider 的初始化 init，由 ir 调用
   */
  open fun <T : Any> addKClassProvider(clazz: KClass<T>, name: String, init: () -> KClass<out T>) {
    val wrapper = KClassProviderWrapper(name, clazz, init)
    val oldWrapper = KClassProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
    if (oldWrapper != null) {
      val clazzInfo = if (clazz != Nothing::class) "，clazz=${clazz}" else ""
      val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
      throw IllegalStateException("@KClassProvider 注解出现重复$clazzInfo$nameInfo")
    }
  }
  
  companion object {
    // 用于 KtProviderManager 使用，当然你也可以使用他们实现自己路由管理类
    protected val ImplProviderMapInternal = LinkedHashMap<KClass<*>, LinkedHashMap<String, ImplProviderWrapper<*>>>()
    protected val KClassProviderMapInternal = LinkedHashMap<KClass<*>, LinkedHashMap<String, KClassProviderWrapper<*>>>()
    
    val ImplProviderMap: Map<KClass<*>, Map<String, ImplProviderWrapper<*>>>
      get() = ImplProviderMapInternal
    val KClassProviderMap: Map<KClass<*>, Map<String, KClassProviderWrapper<*>>>
      get() = KClassProviderMapInternal
  }
}