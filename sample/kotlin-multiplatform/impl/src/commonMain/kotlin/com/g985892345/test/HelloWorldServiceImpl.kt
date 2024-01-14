package com.g985892345.test

import com.IHelloWorldService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:45
 */
@ImplProvider
object HelloWorldServiceImpl : IHelloWorldService {
  override fun get(): String {
    return "Hello world!"
  }
}