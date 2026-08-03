package com.g985892345.provider.api.init

import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import kotlin.reflect.KClass

/**
 * Read-only provider registry used by provider lookups.
 *
 * Registration remains on [IKtProviderDelegate], allowing generated routers to keep their existing API.
 */
interface IKtProviderResolver {

  /** Registered implementation providers grouped by service class and provider name. */
  val implProviders: Map<KClass<*>?, Map<String, ImplProviderWrapper<*>>>

  /** Registered implementation classes grouped by service class and provider name. */
  val kClassProviders: Map<KClass<*>?, Map<String, KClassProviderWrapper<*>>>
}
