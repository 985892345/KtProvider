import com.IImplService
import com.IKClassService
import com.g985892345.provider.sample.kotlinjvm.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProvider

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:07
 */
fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider() // init service
  
  judge {
    KtProvider.impl(IImplService::class, "class1").get() ===
        KtProvider.impl(IImplService::class, "class2").get()
  }
  
  judge {
    val defaultObjectImplServiceImpl = KtProvider.impl(IImplService::class)
    arrayOf(
      KtProvider.impl(IImplService::class, "object1"),
      KtProvider.impl(IImplService::class, "object2"),
    ).all {
      it.get() === defaultObjectImplServiceImpl.get()
    }
  }
  
  judge {
    val defaultKClassImplServiceImpl = KtProvider.clazz(IKClassService::class)
    arrayOf(
      KtProvider.clazz(IKClassService::class, "KClass1"),
      KtProvider.clazz(IKClassService::class, "KClass2"),
    ).all {
      it === defaultKClassImplServiceImpl
    }
  }
  println(KtProvider.impl(IImplService::class, name = "test").get())
}

private fun judge(action: () -> Boolean) {
  if (!action.invoke()) {
    error("???")
  }
}
