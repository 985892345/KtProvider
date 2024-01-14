package com.g985892345.test

import com.IImplService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/9 13:44
 */
@ImplProvider
@ImplProvider(clazz = IImplService::class, name = "object1")
@ImplProvider(clazz = IImplService::class, name = "object2")
object ObjectImplServiceImpl : IImplService {
  override fun get(): Any {
    return this
  }
}