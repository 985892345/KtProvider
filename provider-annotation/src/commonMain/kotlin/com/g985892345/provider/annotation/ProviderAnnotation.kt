package com.g985892345.provider.annotation

import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 20:21
 */

/**
 * 每次获取都是新的实例
 * - 只支持带有无参构造器的普通 Class
 * - class 和 name 变量必须设置一个，也可以全都设置
 * - 因 Kotlin/Js 不支持 KClass.qualifiedName，只能使用 simpleName，所以要求接口类名不能相同（暂时未想到好的替代方案）
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NewImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * 每次获取都是单例
 * - 支持带有无参构造器的普通 Class 和 object 单例类
 * - class 和 name 变量必须设置一个，也可以全都设置
 * - 因 Kotlin/Js 不支持 KClass.qualifiedName，只能使用 simpleName，所以要求接口类名不能相同（暂时未想到好的替代方案）
 * - 如果一个类被打上多个 @SingleImplProvider，则在不是 object 单例类时，每个注解获取到的实例会不相同
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * 获取实现类的 KClass
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KClassProvider(val name: String)
