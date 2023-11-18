package com.g985892345.test

import ITestService
import com.g985892345.provider.annotation.ImplProvider
import com.g985892345.provider.annotation.KClassProvider

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:05
 */
// @NewImplProvider(clazz = ITestService::class)
// class TestServiceImpl : ITestService {
//   override fun get(): String {
//     return toString()
//   }
// }

@ImplProvider
class TestServiceImpl3 : ITestService {
  override fun get(): String {
    return toString()
  }
}

@ImplProvider(clazz = ITestService::class, "single")
object SingleTestServiceImpl : ITestService {
  override fun get(): String {
    return toString()
  }
}

@KClassProvider
@KClassProvider(name = "class")
class KClassTestServiceImpl : ITestService {
  override fun get(): String {
    return toString()
  }
}