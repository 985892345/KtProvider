import com.IImplService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/23 18:30
 */
@ImplProvider(clazz = IImplService::class, name = "test")
object TestServiceImpl : IImplService {
  override fun get(): Any {
    return "123"
  }
}