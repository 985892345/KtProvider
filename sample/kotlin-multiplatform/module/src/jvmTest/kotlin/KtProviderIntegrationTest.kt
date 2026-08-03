import com.IHelloWorldService
import com.IImplService
import com.IKClassService
import com.g985892345.provider.ktprovider.sample.kotlinmultiplatform.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KtProviderIntegrationTest {

  @BeforeTest
  fun initializeProviders() {
    ModuleKtProviderInitializer.tryInitKtProvider()
  }

  @Test
  fun loadsProvidersFromDependentModule() {
    assertEquals(
      "Hello world!",
      KtProvider.impl(IHelloWorldService::class).get(),
    )
  }

  @Test
  fun generatedClassProvidersCreateFreshInstances() {
    val first = KtProvider.impl(IImplService::class, "class1")
    val second = KtProvider.impl(IImplService::class, "class2")

    assertNotSame(first, second)
    assertEquals(first.get(), second.get())
  }

  @Test
  fun generatedObjectProvidersReturnTheSameInstance() {
    val defaultImplementation = KtProvider.impl(IImplService::class)
    val firstAlias = KtProvider.impl(IImplService::class, "object1")
    val secondAlias = KtProvider.impl(IImplService::class, "object2")

    assertSame(defaultImplementation, firstAlias)
    assertSame(defaultImplementation, secondAlias)
  }

  @Test
  fun generatedKClassProvidersReturnTheSameClass() {
    val defaultClass = KtProvider.clazz(IKClassService::class)

    assertEquals(
      defaultClass,
      KtProvider.clazz(IKClassService::class, "KClass1"),
    )
    assertEquals(
      defaultClass,
      KtProvider.clazz(IKClassService::class, "KClass2"),
    )
  }

  @Test
  fun dependencyRegistrationsAreVisibleThroughBulkQueries() {
    val implementationNames = KtProvider.allImpl(IImplService::class).keys
    val classNames = KtProvider.allClazz(IKClassService::class).keys

    assertTrue(implementationNames.containsAll(listOf("", "class1", "class2", "object1", "object2")))
    assertTrue(classNames.containsAll(listOf("", "KClass1", "KClass2")))
  }
}
