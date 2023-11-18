package com.g985892345.test

import ITestService
import com.g985892345.provider.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/10/7 23:40
 */
@ImplProvider(ITestService::class, "555")
class Impl1 : ITestService {
  override fun get(): String {
    return "111"
  }
}