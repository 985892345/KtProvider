package com.g985892345.test

import ITestService
import com.g985892345.provider.annotation.KClassProvider
import com.g985892345.provider.annotation.NewImplProvider
import com.g985892345.provider.annotation.SingleImplProvider

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

@NewImplProvider
class TestServiceImpl3 : ITestService {
  override fun get(): String {
    return toString()
  }
}

@SingleImplProvider(clazz = ITestService::class, "single")
object SingleTestServiceImpl : ITestService {
  override fun get(): String {
    return toString()
  }
}

@KClassProvider("class")
class KClassTestServiceImpl : ITestService {
  override fun get(): String {
    return toString()
  }
}