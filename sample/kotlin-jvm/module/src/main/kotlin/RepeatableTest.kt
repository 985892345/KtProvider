import com.ITestService
import com.ITestService2
import com.g985892345.provider.api.annotation.ImplProvider
import com.g985892345.provider.ktprovider.sample.kotlinjvm.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2023/10/30 16:45
 */
@ImplProvider(ITestService::class, "zzz")
@ImplProvider(ITestService2::class, "zzz")
object RepeatableTest : ITestService, ITestService2 {
  override fun get(): String {
    return ""
  }
}

fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider()
  val service1 = KtProviderManager.getImplOrThrow(ITestService::class, "zzz")
  val service2 = KtProviderManager.getImplOrThrow(ITestService2::class, "zzz")
  println(".(${Exception().stackTrace[0].run { "$fileName:$lineNumber" }}) -> " +
    "service1 = $service1, service2 = $service2")
}