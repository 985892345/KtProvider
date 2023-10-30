package com.g985892345.provider.annotation

import kotlin.reflect.KClass

/**
 * 所有注解
 *
 * @author 985892345
 * 2023/6/13 20:21
 */

/**
 * 每次获取都是新的实例
 * - 只支持带有无参构造器的非抽象 class
 * - [clazz] 和 [name] 最少设置一个
 * - 在不填写 [name] 时，如果直接父类型只有一个接口或者只继承了类时，则可以不填写 [clazz]
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NewImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * 每次获取都是单例
 * - 支持带有无参构造器的非抽象 class 和 object 单例类
 * - [clazz] 和 [name] 最少设置一个
 * - 在不填写 [name] 时，如果直接父类型只有一个接口或者只继承了类时，则可以不填写 [clazz]
 *
 * ## 注意
 * - 如果一个类被打上多个 @SingleImplProvider，则在不是 object 单例类时，每个注解获取到的实例会不相同
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * 获取实现类的 KClass
 * - 支持 Class、object、接口
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KClassProvider(val name: String)
