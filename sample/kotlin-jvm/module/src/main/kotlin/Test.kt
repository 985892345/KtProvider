import com.ITestService
import com.g985892345.provider.ktprovider.sample.kotlinjvm.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:07
 */
fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider() // 初始化服务
  val service1 = KtProviderManager.getImplOrThrow(ITestService::class)
  println(service1.get())
  val allImplList = KtProviderManager.getAllImpl(ITestService::class)
  println("ImplList: $allImplList -> ${allImplList.map { it.value.get() }}")
  val kClassList = KtProviderManager.getAllKClass(ITestService::class)
  println("KClassList: $kClassList -> ${kClassList.map { it.value.get() }}")

  println()
}
