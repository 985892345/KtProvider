package com.g985892345.provider.api.init.wrapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ProviderWrapperTest {

  @Test
  fun implWrapperInvokesFactoryForEveryLookup() {
    var sequence = 0
    val wrapper = ImplProviderWrapper("service", TestService::class) {
      TestServiceImpl(++sequence)
    }

    val first = wrapper.get()
    val second = wrapper.get()

    assertEquals("service", wrapper.name)
    assertEquals(TestService::class, wrapper.clazz)
    assertEquals(1, first.id)
    assertEquals(2, second.id)
    assertNotSame(first, second)
  }

  @Test
  fun implWrapperCanReturnSingletonFromFactory() {
    val singleton = TestServiceImpl(1)
    val wrapper = ImplProviderWrapper("singleton", TestService::class) { singleton }

    assertSame(singleton, wrapper.get())
    assertSame(singleton, wrapper.get())
  }

  @Test
  fun kClassWrapperReturnsConfiguredImplementationClass() {
    val wrapper = KClassProviderWrapper("class", TestService::class) {
      TestServiceImpl::class
    }

    assertEquals("class", wrapper.name)
    assertEquals(TestService::class, wrapper.clazz)
    assertEquals(TestServiceImpl::class, wrapper.get())
  }

  private interface TestService {
    val id: Int
  }

  private class TestServiceImpl(override val id: Int) : TestService
}
