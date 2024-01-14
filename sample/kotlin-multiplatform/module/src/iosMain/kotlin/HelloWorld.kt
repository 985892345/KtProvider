import com.IHelloWorldService
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:46
 */

fun getHelloWorld(): String {
  commonMain()
  return KtProviderManager.getImplOrThrow(IHelloWorldService::class).get()
}