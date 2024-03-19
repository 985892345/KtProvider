package com.g985892345.provider.api.init

/**
 * .
 *
 * @author 985892345
 * 2023/12/4 21:41
 */
abstract class KtProviderRouter {
  
  abstract fun initRouter(delegate: IKtProviderDelegate)
  
  companion object {
    val Empty = object : KtProviderRouter() {
      override fun initRouter(delegate: IKtProviderDelegate) {}
    }
  }
}