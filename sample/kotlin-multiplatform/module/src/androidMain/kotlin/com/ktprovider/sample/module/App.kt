package com.ktprovider.sample.module

import android.app.Application
import com.g985892345.provider.sample.kotlinmultiplatform.module.ModuleKtProviderInitializer

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:04
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    ModuleKtProviderInitializer.tryInitKtProvider() // init service
  }
}