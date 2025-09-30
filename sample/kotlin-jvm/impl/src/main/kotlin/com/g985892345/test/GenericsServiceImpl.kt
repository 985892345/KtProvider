package com.g985892345.test

import com.IGenericsService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2025/4/4 22:17
 */
@ImplProvider
class GenericsServiceImpl : IGenericsService<Any> {
  override fun get(): Boolean {
    return true
  }
}