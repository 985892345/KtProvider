package com.g985892345.provider.init

import kotlin.reflect.KClass

/**
 * KtProviderInitializer 实现类代理接口，在 build.gradle.kts 中 ktProvider 闭包进行设置
 *
 * @author 985892345
 * 2023/11/18 22:23
 */
interface IKtProviderInitializerDelegate {
  
  /**
   * 在 super.initKtProvider() 之前调用，此时未初始化当前模块的路由
   */
  fun onSuperInitKtProviderBefore() {}
  
  /**
   * 在已添加自身路由，但未添加其他模块路由时调用
   */
  fun onSelfAllProviderFinish() {}
  
  /**
   * 在 super.initKtProvider() 之后调用，此时已初始化所有路由，包括其他模块的路由
   */
  fun onSuperInitKtProviderAfter() {}
  
  /**
   * 代理 addImplProvider 方法，如果需要添加该路由的话返回 true
   */
  fun <T : Any> onAddImplProvider(clazz: KClass<T>, name: String, init: () -> T): Boolean = false
  
  /**
   * 代理 addKClassProvider 方法，如果需要添加该路由的话返回 true
   */
  fun <T : Any> onAddKClassProvider(clazz: KClass<T>, name: String, init: () -> KClass<out T>): Boolean = false
}