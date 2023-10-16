package com.g985892345.test

import ITestService
import com.g985892345.provider.annotation.SingleImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/10/7 23:40
 */
@SingleImplProvider(ITestService::class, "444")
class Impl1 : ITestService {
  override fun get(): String {
    return "111"
  }
}