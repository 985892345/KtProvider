package com.g985892345.test

import com.IKClassService
import com.g985892345.provider.api.annotation.KClassProvider

/**
 * .
 *
 * @author 985892345
 * 2024/1/9 13:45
 */
@KClassProvider
@KClassProvider(clazz = IKClassService::class, name = "KClass1")
@KClassProvider(clazz = IKClassService::class, name = "KClass2")
class KClassServiceImpl : IKClassService {
}