package com.g985892345.test

import ITestService
import ITestService2
import com.g985892345.provider.annotation.SingleImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/7/22 13:41
 */
@SingleImplProvider(ITestService2::class)
object TestServiceImpl2 : ITestService2 {
}

@SingleImplProvider(ITestService::class, "123")
object TestServiceImpl4 : ITestService {
  override fun get(): String {
    return "get"
  }
  
}