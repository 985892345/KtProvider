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
  val service31 = KtProviderManager.getImplOrThrow(ITestService::class, "single")
  println(service31.get())
  val kClass = KtProviderManager.getKClassOrThrow<ITestService>("class")
  println(kClass)
  val allImplList = KtProviderManager.getAllImpl(ITestService::class)
  println("$allImplList -> ${allImplList.map { it.value.get() }}")
  val kClassList = KtProviderManager.getAllKClass(ITestService::class)
  println("$kClassList -> ${kClassList.map { it.value.get() }}")
  println(ModuleKtProviderInitializer::class.members.map { it.name })
  println(ModuleKtProviderInitializer::class.java.methods.map { it.name })

  println()
}
