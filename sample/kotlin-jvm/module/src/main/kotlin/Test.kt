import com.g985892345.provider.KtProvider.sample.kotlinjvm.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:07
 */
fun main() {
  ModuleKtProviderInitializer.initKtProvider() // 初始化服务
  val service1 = KtProviderManager.getImplOrThrow(ITestService::class, singleton = null)
  println(service1.get())
  val service21 = KtProviderManager.getImplOrThrow(ITestService::class, singleton = false)
  println(service21.get())
  val service22 = KtProviderManager.getImplOrThrow(ITestService::class, singleton = false)
  println(service22.get())
  val service31 = KtProviderManager.getImplOrThrow(ITestService::class, "single", singleton = true)
  println(service31.get())
  val service32 = KtProviderManager.getImplOrThrow(ITestService::class, "single", singleton = true)
  println(service32.get())
  val kClass = KtProviderManager.getKClassOrThrow<ITestService>("class")
  println(kClass)
  val singleImplList = KtProviderManager.getAllSingleImpl(ITestService::class)
  println("$singleImplList -> ${singleImplList.map { it.value.invoke() }}")
  val newImplList = KtProviderManager.getAllNewImpl(ITestService::class)
  println("$newImplList -> ${newImplList.map { it.value.invoke() }}")
  println(ModuleKtProviderInitializer::class.members.map { it.name })
  println(ModuleKtProviderInitializer::class.java.methods.map { it.name })

  println()
}
