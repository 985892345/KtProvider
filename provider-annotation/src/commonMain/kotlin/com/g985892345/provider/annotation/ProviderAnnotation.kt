package com.g985892345.provider.annotation

import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 20:21
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NewImplProvider(val clazz: KClass<*> = Any::class, val name: String = "")

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleImplProvider(val clazz: KClass<*> = Any::class, val name: String = "")

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KClassProvider(val name: String)
