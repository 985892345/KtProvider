package com.g985892345.test

import com.IImplService
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/9 13:44
 */
@ImplProvider(clazz = IImplService::class, name = "class1")
@ImplProvider(clazz = IImplService::class, name = "class2")
class ClassImplServiceImpl: IImplService {
  override fun get(): Any {
    return ClassImplServiceImpl::class
  }
}