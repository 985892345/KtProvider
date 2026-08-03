package com.g985892345.provider.manager

import com.g985892345.provider.api.init.IKtProviderDelegate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KtProviderTest {

  @Test
  fun retrievesFreshImplementationsAndExposesAllRegistrations() {
    var sequence = 0
    IKtProviderDelegate.addImplProvider(FactoryService::class, "") {
      FactoryServiceImpl(++sequence)
    }
    IKtProviderDelegate.addImplProvider(FactoryService::class, "secondary") {
      FactoryServiceImpl(++sequence)
    }

    val first = KtProvider.impl(FactoryService::class)
    val second = KtProvider.impl(FactoryService::class)
    val registrations = KtProvider.allImpl(FactoryService::class)

    assertEquals(1, first.id)
    assertEquals(2, second.id)
    assertNotSame(first, second)
    assertEquals(setOf("", "secondary"), registrations.keys)
    assertEquals(3, registrations.getValue("secondary").get().id)
  }

  @Test
  fun retrievesImplementationRegisteredOnlyByName() {
    val implementation = NamedServiceImpl
    IKtProviderDelegate.addImplProvider(null, "manager-test-named") {
      implementation
    }

    assertSame(implementation, KtProvider.impl<NamedService>("manager-test-named"))
  }

  @Test
  fun retrievesImplementationClassAndExposesAllRegistrations() {
    IKtProviderDelegate.addKClassProvider(ClassService::class, "") {
      ClassServiceImpl::class
    }
    IKtProviderDelegate.addKClassProvider(ClassService::class, "secondary") {
      AlternativeClassServiceImpl::class
    }

    assertEquals(ClassServiceImpl::class, KtProvider.clazz(ClassService::class))
    assertEquals(
      AlternativeClassServiceImpl::class,
      KtProvider.clazz(ClassService::class, "secondary"),
    )
    assertEquals(
      setOf("", "secondary"),
      KtProvider.allClazz(ClassService::class).keys,
    )
  }

  @Test
  fun nullableLookupsReturnNullAndRequiredLookupsFail() {
    assertNull(KtProvider.implOrNull(MissingService::class))
    assertNull(KtProvider.clazzOrNull(MissingService::class))
    assertFailsWith<NullPointerException> {
      KtProvider.impl(MissingService::class)
    }
    assertFailsWith<NullPointerException> {
      KtProvider.clazz(MissingService::class)
    }
  }

  @Test
  fun nameOnlyLookupRejectsEmptyName() {
    assertFailsWith<IllegalArgumentException> {
      KtProvider.implOrNull<Any>("")
    }
    assertFailsWith<IllegalArgumentException> {
      KtProvider.clazzOrNull<Any>("")
    }
  }

  @Test
  fun duplicateRegistrationFailsFast() {
    IKtProviderDelegate.addImplProvider(DuplicateService::class, "") {
      DuplicateServiceImpl
    }

    val error = assertFailsWith<IllegalStateException> {
      IKtProviderDelegate.addImplProvider(DuplicateService::class, "") {
        DuplicateServiceImpl
      }
    }

    assertNotNull(error.message)
    assertTrue(error.message!!.contains("Duplicate @ImplProvider"))
  }

  private interface FactoryService {
    val id: Int
  }

  private data class FactoryServiceImpl(override val id: Int) : FactoryService

  private interface NamedService

  private object NamedServiceImpl : NamedService

  private interface ClassService

  private class ClassServiceImpl : ClassService

  private class AlternativeClassServiceImpl : ClassService

  private interface MissingService

  private interface DuplicateService

  private object DuplicateServiceImpl : DuplicateService
}
