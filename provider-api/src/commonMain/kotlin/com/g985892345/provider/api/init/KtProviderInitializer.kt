package com.g985892345.provider.api.init

/**
 * Initialization
 *
 * Kotlin/Jvm: It is recommended to perform initialization in the main function.
 * ```
 * fun main() {
 *   // Invoke the automatically generated XXXKtProviderInitializer class (module name + KtProviderInitializer)
 *   // This class will be automatically generated during the build process.
 *   // Alternatively, you can directly invoke the generateXXXKtProviderInitializerImpl Gradle task to generate it.
 *   XXXKtProviderInitializer.tryInitKtProvider()
 * }
 * ```
 *
 * Android: It is recommended to perform initialization in the Application#onCreate method.
 * ```
 * class App : Application() {
 *   override fun onCreate() {
 *     super.onCreate()
 *     XXXKtProviderInitializer.tryInitKtProvider()
 *   }
 * }
 * ```
 *
 * iOS: Initialize in App#init (Swift) or application:didFinishLaunchingWithOptions: (Objective-C) (I'm not proficient in iOS, so this may not be the most optimal approach).
 * ```
 * @main
 * struct iOSApp: App {
 *     init() {
 *         XXXKtProviderInitializer.tryInitKtProvider()
 *     }
 * }
 * - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
 *     XXXKtProviderInitializer.tryInitKtProvider()
 * }
 * ```
 *
 * @author 985892345
 * 2023/6/14 11:37
 */
abstract class KtProviderInitializer {
  
  /**
   * Prevent duplicate loading.
   */
  private var mHasInit = false
  
  fun tryInitKtProvider() {
    tryInitKtProvider(IKtProviderDelegate)
  }
  
  fun tryInitKtProvider(delegate: IKtProviderDelegate) {
    if (mHasInit) return
    mHasInit = true
    initKtProvider(delegate)
  }
  
  protected open fun initKtProvider(delegate: IKtProviderDelegate) {
    delegate.onInitKtProviderBefore(this)
    router.initRouter(delegate)
    delegate.onSelfAllProviderFinish(this)
    otherModuleKtProvider.forEach {
      it.tryInitKtProvider(delegate)
    }
    delegate.onInitKtProviderAfter(this)
  }
  
  protected abstract val router: KtProviderRouter
  
  protected abstract val otherModuleKtProvider: List<KtProviderInitializer>
}
