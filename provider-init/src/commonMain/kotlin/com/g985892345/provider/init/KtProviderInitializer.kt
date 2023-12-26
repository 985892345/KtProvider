package com.g985892345.provider.init

/**
 * 初始化服务
 *
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
abstract class KtProviderInitializer {
  
  private var mHasInit = false
  
  /**
   * 防止重复加载
   */
  fun tryInitKtProvider(delegate: IKtProviderDelegate = IKtProviderDelegate) {
    if (mHasInit) return
    mHasInit = true
    initKtProvider(delegate)
  }
  
  protected open fun initKtProvider(delegate: IKtProviderDelegate) {
    delegate.onInitKtProviderBefore(this)
    router.initRouter(delegate)
    delegate.onSelfAllProviderFinish(this)
    otherModuleKtProvider.forEach {
      it.tryInitKtProvider(delegate)
    }
    delegate.onInitKtProviderAfter(this)
  }
  
  protected abstract val router: KtProviderRouter
  
  protected abstract val otherModuleKtProvider: List<KtProviderInitializer>
}
