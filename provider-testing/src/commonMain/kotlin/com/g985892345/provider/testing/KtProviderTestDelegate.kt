package com.g985892345.provider.testing

import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.IKtProviderInitializationTracker
import com.g985892345.provider.api.init.IKtProviderResolver
import com.g985892345.provider.api.init.KtProviderInitializer
import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * Isolated provider registry used by a single test scope.
 *
 * Generated routers register into this delegate exactly as they register into the production
 * delegate. Overrides replace entries only in this registry and never mutate production state.
 */
class KtProviderTestDelegate :
  IKtProviderDelegate,
  IKtProviderResolver,
  IKtProviderInitializationTracker {

  private val implProvidersInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, ImplProviderWrapper<*>>>()
  private val kClassProvidersInternal = LinkedHashMap<KClass<*>?, LinkedHashMap<String, KClassProviderWrapper<*>>>()
  private val initializedProviders = mutableListOf<KtProviderInitializer>()

  override val implProviders: Map<KClass<*>?, Map<String, ImplProviderWrapper<*>>>
    get() = implProvidersInternal

  override val kClassProviders: Map<KClass<*>?, Map<String, KClassProviderWrapper<*>>>
    get() = kClassProvidersInternal

  override fun markKtProviderInitialized(initializer: KtProviderInitializer): Boolean {
    if (initializedProviders.any { it === initializer }) return false
    initializedProviders += initializer
    return true
  }

  override fun <T : Any> addImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {
    val wrapper = ImplProviderWrapper(name, clazz, init)
    val previous = implProvidersInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
    check(previous == null) {
      "Duplicate @ImplProvider annotation found: (clazz=$clazz, name=$name)"
    }
  }

  override fun <T : Any> addKClassProvider(
    clazz: KClass<T>?,
    name: String,
    init: () -> KClass<out T>,
  ) {
    val wrapper = KClassProviderWrapper(name, clazz, init)
    val previous = kClassProvidersInternal.getOrPut(clazz) { linkedMapOf() }.put(name, wrapper)
    check(previous == null) {
      "Duplicate @KClassProvider annotation found: (clazz=$clazz, name=$name)"
    }
  }

  /** Replaces an implementation provider in this test registry. */
  fun <T : Any> overrideImplProvider(clazz: KClass<T>?, name: String, init: () -> T) {
    implProvidersInternal.getOrPut(clazz) { linkedMapOf() }[name] = ImplProviderWrapper(name, clazz, init)
  }

  /** Replaces an implementation-class provider in this test registry. */
  fun <T : Any> overrideKClassProvider(
    clazz: KClass<T>?,
    name: String,
    init: () -> KClass<out T>,
  ) {
    kClassProvidersInternal.getOrPut(clazz) { linkedMapOf() }[name] = KClassProviderWrapper(name, clazz, init)
  }
}
