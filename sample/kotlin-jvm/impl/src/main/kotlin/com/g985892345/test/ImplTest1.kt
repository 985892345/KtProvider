package com.g985892345.test

import com.ITestService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2023/12/13 16:48
 */
@ImplProvider
class ImplTest1 : ITestService {
  override fun get(): String {
    return ""
  }
}
