package com.g985892345.provider.api.annotation

import kotlin.reflect.KClass

/**
 * Annotation
 *
 * @author 985892345
 * 2023/6/13 20:21
 */

/**
 * Obtain an instance
 * - For class implementation, a new instance is created each time it is retrieved.
 * - For object implementation, a singleton instance is returned each time it is retrieved.
 * - At least one of [clazz] and [name] should be set.
 * - If the immediate parent type has only one interface or class, [clazz] and [name] can be omitted,
 *   indicating the default implementation class for [clazz].
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ImplProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")

/**
 * Retrieve the KClass of the implementation class
 * - Supports Class, object, and interface.
 * - At least one of [clazz] and [name] should be set.
 * - If the immediate parent type has only one interface or class, [clazz] and [name] can be omitted,
 *   indicating the default implementation class for [clazz].
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KClassProvider(val clazz: KClass<*> = Nothing::class, val name: String = "")
