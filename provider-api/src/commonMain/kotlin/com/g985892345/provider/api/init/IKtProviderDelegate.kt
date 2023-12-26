package com.g985892345.provider.api.init

import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * KtProviderInitializer 实现类代理接口，在 build.gradle.kts 中 ktProvider 闭包进行设置
 *
 * @author 985892345
 * 2023/11/18 22:23
 */
interface IKtProviderDelegate {
  
  /**
   * 在 initKtProvider() 之前调用，此时未初始化当前模块的路由
   */
  fun onInitKtProviderBefore(ktProvider: KtProviderInitializer) {}
  
  /**
   * 在已添加自身路由，但未添加其他模块路由时调用
   */
  fun onSelfAllProviderFinish(ktProvider: KtProviderInitializer) {}
  
  /**
   * 在 initKtProvider() 之后调用，此时已初始化所有路由，包括其他模块的路由
   */
  fun onInitKtProviderAfter(ktProvider: KtProviderInitializer) {}
  
  /**
   *  addImplProvider 方法
   */
  fun <T : Any> addImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {}
  
  /**
   * addKClassProvider 方法
   */
  fun <T : Any> addKClassProvider(clazz: KClass<T>?, name: String, init: () -> KClass<out T>) {}
  
  companion object : IKtProviderDelegate {
    
    // 添加进来的所有路由信息
    private val ImplProviderMapInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, ImplProviderWrapper<*>>>()
    private val KClassProviderMapInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, KClassProviderWrapper<*>>>()
    val ImplProviderMap: Map<KClass<*>?, Map<String, ImplProviderWrapper<*>>>
      get() = ImplProviderMapInternal
    val KClassProviderMap: Map<KClass<*>?, Map<String, KClassProviderWrapper<*>>>
      get() = KClassProviderMapInternal
    
    /**
     * 添加一个 @ImplProvider 的初始化 init
     */
    override fun <T : Any> addImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {
      val wrapper = ImplProviderWrapper(name, clazz, init)
      val oldWrapper = ImplProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
      if (oldWrapper != null) {
        val clazzInfo = if (clazz != Nothing::class) "，clazz=${clazz}" else ""
        val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
        throw IllegalStateException("@ImplProvider 注解出现重复$clazzInfo$nameInfo")
      }
    }
    
    /**
     * 添加一个 @KClassProvider 的初始化 init
     */
    override fun <T : Any> addKClassProvider(clazz: KClass<T>?, name: String, init: () -> KClass<out T>) {
      val wrapper = KClassProviderWrapper(name, clazz, init)
      val oldWrapper = KClassProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
      if (oldWrapper != null) {
        val clazzInfo = if (clazz != Nothing::class) "，clazz=${clazz}" else ""
        val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
        throw IllegalStateException("@KClassProvider 注解出现重复$clazzInfo$nameInfo")
      }
    }
  }
}