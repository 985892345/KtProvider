package com.g985892345.test

import ITestService
import ITestService2
import com.g985892345.provider.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/7/22 13:41
 */
@ImplProvider
object TestServiceImpl2 : ITestService2 {
}

@ImplProvider(ITestService::class, "333")
object TestServiceImpl4 : ITestService {
  override fun get(): String {
    return "get"
  }
  
}