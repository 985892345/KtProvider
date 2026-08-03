package com.g985892345.provider.testing

import com.g985892345.provider.api.init.KtProviderInitializer
import com.g985892345.provider.manager.KtProvider
import com.g985892345.provider.manager.KtProviderTestingApi
import kotlin.reflect.KClass

/**
 * Test scope that exposes isolated provider overrides while production lookups are redirected.
 */
class KtProviderTestScope internal constructor(
  val delegate: KtProviderTestDelegate,
) {

  /** Overrides an implementation provider for [T] in the current test scope. */
  inline fun <reified T : Any> overrideImpl(name: String = "", noinline init: () -> T) {
    delegate.overrideImplProvider(T::class, name, init)
  }

  /** Overrides an implementation-class provider for [T] in the current test scope. */
  inline fun <reified T : Any> overrideKClass(
    name: String = "",
    noinline init: () -> KClass<out T>,
  ) {
    delegate.overrideKClassProvider(T::class, name, init)
  }
}

/**
 * Loads [initializer] and all generated dependency initializers into an isolated registry, then
 * redirects [KtProvider] lookups for the duration of [block].
 *
 * The block is synchronous. Do not overlap scopes on the same platform thread.
 */
@OptIn(KtProviderTestingApi::class)
fun <R> withKtProviderTest(
  initializer: KtProviderInitializer,
  block: KtProviderTestScope.() -> R,
): R {
  val delegate = KtProviderTestDelegate()
  initializer.tryInitKtProvider(delegate)
  val scope = KtProviderTestScope(delegate)
  return KtProvider.withResolver(delegate) {
    scope.block()
  }
}
