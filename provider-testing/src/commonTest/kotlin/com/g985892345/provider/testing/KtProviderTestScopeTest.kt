package com.g985892345.provider.testing

import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.KtProviderInitializer
import com.g985892345.provider.api.init.KtProviderRouter
import com.g985892345.provider.manager.KtProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KtProviderTestScopeTest {

  @Test
  fun loadsDependenciesAndAppliesScopedOverrides() {
    val dependency = TestInitializer(
      registrations = { delegate ->
        delegate.addImplProvider(TestService::class, "") { DefaultTestService }
        delegate.addKClassProvider(TestService::class, "") { DefaultTestService::class }
      },
    )
    val module = TestInitializer(otherModuleKtProvider = listOf(dependency))

    withKtProviderTest(module) {
      overrideImpl<TestService> { FakeTestService }
      overrideKClass<TestService> { FakeTestService::class }

      assertEquals("fake", KtProvider.impl(TestService::class).value)
      assertEquals(FakeTestService::class, KtProvider.clazz(TestService::class))
    }

    assertNull(KtProvider.implOrNull(TestService::class))
    assertNull(KtProvider.clazzOrNull(TestService::class))
  }

  @Test
  fun generatedSingletonInitializerCanPopulateEachTestRegistry() {
    val initializer = TestInitializer(
      registrations = { delegate ->
        delegate.addImplProvider(TestService::class, "") { DefaultTestService }
      },
    )

    repeat(2) {
      withKtProviderTest(initializer) {
        assertEquals("default", KtProvider.impl(TestService::class).value)
      }
    }
  }

  @Test
  fun restoresProductionResolverWhenTestBlockFails() {
    val initializer = TestInitializer(
      registrations = { delegate ->
        delegate.addImplProvider(TestService::class, "") { DefaultTestService }
      },
    )

    assertFailsWith<IllegalStateException> {
      withKtProviderTest(initializer) {
        assertEquals("default", KtProvider.impl(TestService::class).value)
        error("expected failure")
      }
    }

    assertNull(KtProvider.implOrNull(TestService::class))
  }

  private interface TestService {
    val value: String
  }

  private object DefaultTestService : TestService {
    override val value = "default"
  }

  private object FakeTestService : TestService {
    override val value = "fake"
  }

  private class TestInitializer(
    private val registrations: (IKtProviderDelegate) -> Unit = {},
    override val otherModuleKtProvider: List<KtProviderInitializer> = emptyList(),
  ) : KtProviderInitializer() {

    override val router = object : KtProviderRouter() {
      override fun initRouter(delegate: IKtProviderDelegate) {
        registrations(delegate)
      }
    }
  }
}
