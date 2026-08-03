package com.g985892345.provider.api.init

/**
 * Tracks initializer visits for delegates that own an isolated provider registry.
 *
 * The tracker is responsible for identity-based deduplication. This keeps generated singleton
 * initializers reusable across independent test registries without retaining those registries.
 */
interface IKtProviderInitializationTracker {

  /**
   * Marks [initializer] as visited.
   *
   * @return true when the initializer should run, or false when it was already visited.
   */
  fun markKtProviderInitialized(initializer: KtProviderInitializer): Boolean
}
