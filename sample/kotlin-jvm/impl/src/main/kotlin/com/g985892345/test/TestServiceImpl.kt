package com.g985892345.test

import ITestService
import com.g985892345.provider.annotation.NewImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/6/20 22:05
 */
@NewImplProvider(clazz = ITestService::class)
class TestServiceImpl : ITestService {
  override fun get(): String {
    return "123"
  }
}