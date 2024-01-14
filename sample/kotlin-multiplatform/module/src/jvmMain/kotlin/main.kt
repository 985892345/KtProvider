import com.IHelloWorldService
import com.g985892345.provider.ktprovider.sample.kotlinmultiplatform.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:34
 */
fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider() // init service
  commonMain()
  println(KtProviderManager.getImplOrThrow(IHelloWorldService::class).get())
}