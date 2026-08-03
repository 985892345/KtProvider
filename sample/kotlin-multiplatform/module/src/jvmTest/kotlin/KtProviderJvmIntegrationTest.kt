import com.IImplService
import com.g985892345.provider.ktprovider.sample.kotlinmultiplatform.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KtProviderJvmIntegrationTest {

  @BeforeTest
  fun initializeProviders() {
    ModuleKtProviderInitializer.tryInitKtProvider()
  }

  @Test
  fun loadsProviderGeneratedFromJvmSourceSet() {
    assertEquals(
      "123",
      KtProvider.impl(IImplService::class, "test").get(),
    )
  }
}
