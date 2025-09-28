package com.g985892345.provider.api.init

import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * Proxy for the implementation class of KtProviderInitializer.
 *
 * @author 985892345
 * 2023/11/18 22:23
 */
interface IKtProviderDelegate {
  
  /**
   * Callback at the beginning of initKtProvider(), at this point, the routing for the current module is not yet initialized.
   */
  fun onInitKtProviderBefore(ktProvider: KtProviderInitializer) {}
  
  /**
   * Callback when the self-route is added but other module routes are not yet added.
   */
  fun onSelfAllProviderFinish(ktProvider: KtProviderInitializer) {}
  
  /**
   * Callback at the end of initKtProvider(), at this point, all routes are initialized, including routes from other modules.
   */
  fun onInitKtProviderAfter(ktProvider: KtProviderInitializer) {}
  
  /**
   * Add an @ImplProvider.
   */
  fun <T : Any> addImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {}
  
  /**
   * Add an @KClassProvider
   */
  fun <T : Any> addKClassProvider(clazz: KClass<T>?, name: String, init: () -> KClass<out T>) {}
  
  companion object : IKtProviderDelegate {
    
    // All added route information.
    private val ImplProviderMapInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, ImplProviderWrapper<*>>>()
    private val KClassProviderMapInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, KClassProviderWrapper<*>>>()
    val ImplProviderMap: Map<KClass<*>?, Map<String, ImplProviderWrapper<*>>>
      get() = ImplProviderMapInternal
    val KClassProviderMap: Map<KClass<*>?, Map<String, KClassProviderWrapper<*>>>
      get() = KClassProviderMapInternal
    
    override fun <T : Any> addImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {
      val wrapper = ImplProviderWrapper(name, clazz, init)
      val oldWrapper = ImplProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
      if (oldWrapper != null) {
        val clazzInfo = if (clazz != Nothing::class) "clazz=${clazz}" else ""
        val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
        val oldNameInfo = if (oldWrapper.name.isNotEmpty()) ", oldName=${oldWrapper.name}" else ""
        throw IllegalStateException("Duplicate @ImplProvider annotation found: "
            + "($clazzInfo$nameInfo), "
            + "($clazzInfo$oldNameInfo)")
      }
    }
    
    override fun <T : Any> addKClassProvider(clazz: KClass<T>?, name: String, init: () -> KClass<out T>) {
      val wrapper = KClassProviderWrapper(name, clazz, init)
      val oldWrapper = KClassProviderMapInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
      if (oldWrapper != null) {
        val clazzInfo = if (clazz != Nothing::class) "clazz=${clazz}" else ""
        val nameInfo = if (name.isNotEmpty()) ", name=${name}" else ""
        val oldNameInfo = if (oldWrapper.name.isNotEmpty()) ", oldName=${oldWrapper.name}" else ""
        throw IllegalStateException("Duplicate @KClassProvider annotation found: "
            + "($clazzInfo$nameInfo), "
            + "($clazzInfo$oldNameInfo)")
      }
    }
  }
}