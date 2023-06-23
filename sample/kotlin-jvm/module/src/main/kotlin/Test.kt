import com.g985892345.provider.init.ProviderInitialize
import com.g985892345.provider.manager.ProviderManager
import com.g985892345.provider.manager.getImplOrNull

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:07
 */
fun main() {
  KtProvider.init() // 初始化服务
  val service = ProviderManager.getImplOrNull<ITestService>()
  println(service)
  println(KtProvider::class.members.map { it.name })
  println(KtProvider::class.java.methods.map { it.name })
}

object KtProvider : ProviderInitialize() {
  override fun init() {
    super.init()
  }
}