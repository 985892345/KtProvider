import com.IImplService
import com.IKClassService
import com.g985892345.provider.ktprovider.sample.kotlinjvm.module.ModuleKtProviderInitializer
import com.g985892345.provider.manager.KtProviderManager

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:07
 */
fun main() {
  ModuleKtProviderInitializer.tryInitKtProvider() // init service
  
  judge {
    KtProviderManager.getImplOrThrow(IImplService::class, "class1").get() ===
        KtProviderManager.getImplOrThrow(IImplService::class, "class2").get()
  }
  
  judge {
    val defaultObjectImplServiceImpl = KtProviderManager.getImplOrThrow(IImplService::class)
    arrayOf(
      KtProviderManager.getImplOrThrow(IImplService::class, "object1"),
      KtProviderManager.getImplOrThrow(IImplService::class, "object2"),
    ).all {
      it.get() === defaultObjectImplServiceImpl.get()
    }
  }
  
  judge {
    val defaultKClassImplServiceImpl = KtProviderManager.getKClassOrThrow(IKClassService::class)
    arrayOf(
      KtProviderManager.getKClassOrThrow(IKClassService::class, "KClass1"),
      KtProviderManager.getKClassOrThrow(IKClassService::class, "KClass2"),
    ).all {
      it === defaultKClassImplServiceImpl
    }
  }
  println(KtProviderManager.getImplOrThrow(IImplService::class, name = "test").get())
}

private fun judge(action: () -> Boolean) {
  if (!action.invoke()) {
    error("???")
  }
}
