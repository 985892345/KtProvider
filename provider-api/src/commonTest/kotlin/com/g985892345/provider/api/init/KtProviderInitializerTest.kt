package com.g985892345.provider.api.init

import kotlin.test.Test
import kotlin.test.assertEquals

class KtProviderInitializerTest {

  @Test
  fun initializesOwnRouterBeforeDependencies() {
    val events = mutableListOf<String>()
    val child = RecordingInitializer("child", events)
    val parent = RecordingInitializer("parent", events, listOf(child))
    val delegate = object : IKtProviderDelegate {
      override fun onInitKtProviderBefore(ktProvider: KtProviderInitializer) {
        events += "${ktProvider.name}:before"
      }

      override fun onSelfAllProviderFinish(ktProvider: KtProviderInitializer) {
        events += "${ktProvider.name}:self-finished"
      }

      override fun onInitKtProviderAfter(ktProvider: KtProviderInitializer) {
        events += "${ktProvider.name}:after"
      }
    }

    parent.tryInitKtProvider(delegate)

    assertEquals(
      listOf(
        "parent:before",
        "parent:router",
        "parent:self-finished",
        "child:before",
        "child:router",
        "child:self-finished",
        "child:after",
        "parent:after",
      ),
      events,
    )
  }

  @Test
  fun initializesEachInitializerOnlyOnce() {
    val events = mutableListOf<String>()
    val initializer = RecordingInitializer("module", events)
    val delegate = object : IKtProviderDelegate {
      override fun onInitKtProviderBefore(ktProvider: KtProviderInitializer) {
        events += "${ktProvider.name}:before"
      }
    }

    initializer.tryInitKtProvider(delegate)
    initializer.tryInitKtProvider(delegate)

    assertEquals(listOf("module:before", "module:router"), events)
  }

  private val KtProviderInitializer.name: String
    get() = (this as RecordingInitializer).name

  private class RecordingInitializer(
    val name: String,
    private val events: MutableList<String>,
    override val otherModuleKtProvider: List<KtProviderInitializer> = emptyList(),
  ) : KtProviderInitializer() {

    override val router = object : KtProviderRouter() {
      override fun initRouter(delegate: IKtProviderDelegate) {
        events += "${name}:router"
      }
    }
  }
}
