package com.g985892345.provider.annotation

import kotlin.reflect.KClass

/**
 * 所有注解
 *
 * @author 985892345
 * 2023/6/13 20:21
 */

/**
 * 获取一个实例
 * - 实现方提供 class 则每次获取都是新的实例，提供 object 则每次获取都是单例
 * - [clazz] 和 [name] 最少设置一个
 * - 在不填写 [name] 时，如果直接父类型只有一个接口或者只继承了类时，则可以不填写 [clazz]，默认使用父类型
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * 获取实现类的 KClass
 * - 支持 Class、object、接口
 * - [clazz] 和 [name] 最少设置一个
 * - 在不填写 [name] 时，如果直接父类型只有一个接口或者只继承了类时，则可以不填写 [clazz]，默认使用父类型
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KClassProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")
