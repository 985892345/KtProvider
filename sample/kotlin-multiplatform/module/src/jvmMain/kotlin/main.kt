import com.IHelloWorldService
import com.IImplService
import com.g985892345.provider.sample.kotlinmultiplatform.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:34
 */
fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider() // init service
  commonMain()
  println(KtProvider.impl(IHelloWorldService::class).get())
  println(KtProvider.impl(IImplService::class, name = "test").get())
}