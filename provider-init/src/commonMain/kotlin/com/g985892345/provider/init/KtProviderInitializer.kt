package com.g985892345.provider.init

import com.g985892345.provider.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.init.wrapper.NewImplProviderWrapper
import com.g985892345.provider.init.wrapper.SingleImplProviderWrapper
import kotlin.reflect.KClass

/**
 * 初始化服务
 *
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
abstract class KtProviderInitializer {
  
  val mNewImplProviderMap = LinkedHashMap<KClass<*>, HashMap<String, NewImplProviderWrapper>>()
  val mSingleImplProviderMap = LinkedHashMap<KClass<*>, HashMap<String, SingleImplProviderWrapper>>()
  val mKClassImplProviderMap = LinkedHashMap<String, KClassProviderWrapper>()
  
  private var mHasInit = false
  
  /**
   * 防止重复加载
   */
  fun tryInitKtProvider() {
    if (mHasInit) return
    mHasInit = true
    initKtProvider()
  }
  
  /**
   * 初始化方法，ir 插桩的地方
   */
  protected open fun initKtProvider() {
    mNewImplProviderMap.forEach { outer ->
      val innerMap = NewImplProviderMapInternal.getOrPut(outer.key) { hashMapOf() }
      outer.value.forEach { inner ->
        if (innerMap.put(inner.key, inner.value) != null) {
          val clazzInfo = if (outer.key != Nothing::class) "，clazz=${outer.key}" else ""
          val nameInfo = if (inner.key.isNotEmpty()) ", name=${inner.key}" else ""
          throw IllegalStateException("@NewImplProvider 注解出现重复$clazzInfo$nameInfo")
        }
      }
    }
    mSingleImplProviderMap.forEach { outer ->
      val innerMap = SingleImplProviderMapInternal.getOrPut(outer.key) { hashMapOf() }
      outer.value.forEach { inner ->
        if (innerMap.put(inner.key, inner.value) != null) {
          val clazzInfo = if (outer.key != Nothing::class) "，clazz=${outer.key}" else ""
          val nameInfo = if (inner.key.isNotEmpty()) ", name=${inner.key}" else ""
          throw IllegalStateException("@SingleImplProvider 注解出现重复$clazzInfo$nameInfo")
        }
      }
    }
    mKClassImplProviderMap.forEach { entry ->
      if (KClassProviderMapInternal.put(entry.key, entry.value) != null) {
        throw IllegalStateException("@SingleImplProvider 注解出现重复，name=${entry.key}, " +
          "class 为 ${entry.value.get<Any>()}")
      }
    }
  }
  
  /**
   * 添加一个 @NewImplProvider 的初始化 init，由 ir 调用
   */
  fun addNewImplProvider(clazz: KClass<*>, name: String, init: () -> Any) {
    mNewImplProviderMap.getOrPut(clazz) { hashMapOf() }[name] = NewImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 @SingleImplProvider 的初始化 init，由 ir 调用
   */
  fun addSingleImplProvider(clazz: KClass<*>, name: String, init: () -> Any) {
    mSingleImplProviderMap.getOrPut(clazz) { hashMapOf() }[name] = SingleImplProviderWrapper(init)
  }
  
  /**
   * 添加一个 @KClassProvider 的初始化 init，由 ir 调用
   */
  fun addKClassProvider(name: String, init: () -> KClass<*>) {
    mKClassImplProviderMap[name] = KClassProviderWrapper(init)
  }
  
  companion object {
    // 用于 KtProviderManager 使用，当然你也可以使用他们实现自己路由管理类
    private val NewImplProviderMapInternal = LinkedHashMap<KClass<*>, HashMap<String, NewImplProviderWrapper>>()
    private val SingleImplProviderMapInternal = LinkedHashMap<KClass<*>, HashMap<String, SingleImplProviderWrapper>>()
    private val KClassProviderMapInternal = LinkedHashMap<String, KClassProviderWrapper>()
    
    val NewImplProviderMap: Map<KClass<*>, Map<String, NewImplProviderWrapper>>
      get() = NewImplProviderMapInternal
    val SingleImplProviderMap: Map<KClass<*>, Map<String, SingleImplProviderWrapper>>
      get() = SingleImplProviderMapInternal
    val KClassProviderMap: Map<String, KClassProviderWrapper>
      get() = KClassProviderMapInternal
  }
}