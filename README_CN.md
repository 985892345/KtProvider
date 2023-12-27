# KtProvider
![Maven Central](https://img.shields.io/maven-central/v/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=release)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=SNAPSHOT)

支持 KMP 的跨模块服务提供框架  
- 支持 KMP 的多模块工程
- 可用于 Compose Multiplatform 中

## 配置
目前还处于测试阶段，未发布稳定包，请先设置 MavenCentral 快照仓库后进行依赖
```kotlin
// setting.gradle.kts
// gradle 插件仓库地址
pluginManagement {
  repositories {
    // ...
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

// 依赖地址
// 这个 dependencyResolutionManagement 为 Android 端的写法，该写法用于统一所有模块依赖
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    // ...
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}
```
如果不使用 `dependencyResolutionManagement` 则采取以下写法
```kotlin
// build.gradle.kts
repositories {
  // mavenCentral 快照仓库
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}
```

在所有模块中引入:
```kotlin
// build.gradle.kts
plugins {
  id("io.github.985892345.KtProvider") version "x.y.z"
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // provider-manager 可选择性添加，你可以实现自己的 provider-manager
      implementation(ktProvider.manager)
    }
  }
}

dependencies {
  // ksp 相关配置请参考官方文档: https://kotlinlang.org/docs/ksp-multiplatform.html
  add("kspCommonMainMetadata", ktProvider.ksp)
  add("kspAndroid", ktProvider.ksp)
  add("kspDesktop", ktProvider.ksp)
  add("kspIosX64", ktProvider.ksp)
  add("kspIosArm64", ktProvider.ksp)
  add("kspIosSimulatorArm64", ktProvider.ksp)
  // ...
  
  // provider-api 依赖已随 gradle 插件一起添加
}
```

### 初始化
Kotlin/Jvm: 建议在 `main` 函数进行初始化
```kotlin
fun main() {
  // 调用自动生成的 XXXKtProviderInitializer (模块名+KtProviderInitializer)
  // 会在构建时自动生成，也可以直接调用 generateXXXKtProviderInitializerImpl gradle 任务直接生成
  XXXKtProviderInitializer.tryInitKtProvider()
}
```

Android: 建议在 `Application#onCreate` 中进行初始化
```kotlin
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    XXXKtProviderInitializer.tryInitKtProvider()
  }
}
```

iOS: 在 `App#init` (Swift) 或 `application:didFinishLaunchingWithOptions:` (Objective-C) 中初始化 

(我不擅长 iOS，该写法可能并不是最优的)
```swift
@main
struct iOSApp: App {
    init() {
        XXXKtProviderInitializer.shared.tryInitKtProvider()
    }
}
```
```objective-c
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [XXXKtProviderInitializer.shared tryInitKtProvider];
}
```

### 使用
添加接口
```kotlin
interface ITestService {
  fun get(): String
}
```

实现接口
```kotlin
@ImplProvider(clazz = ITestService::class, name = "test")
class TestServiceImpl : ITestService {
  override fun get(): String {
    return "123"
  }
}
```
| 注解             |                                                     |
|----------------|-----------------------------------------------------|
| ImplProvider   | 获取实例，实现类为 object 时则为单例                              |
| KClassProvider | 获取实现类的 KClass (可用于 Android 中的 Class\<out Activity>) |

使用接口
```kotlin
val service = KtProviderManager.getImplOrNull(ITestService::class, "test")
println(service.get())
```

## 实现原理
**自动生成 KtProviderInitializer 实现类**

KtProvider 的 gradle 插件会自动生成 `KtProviderInitializer` 的实现类，
然后根据模块之间的依赖关系，自动调用其他模块实现类的 `tryInitKtProvider()` 方法，
所以只需要在启动模块中调用 `tryInitKtProvider()` 方法即可加载全部路由

**（注意：只允许 implementation、api 依赖其他模块）**

**KSP 解析模块内注解**

基于 KSP，解析模块内注解，并生成 `KtProviderRouter` 的实现类，然后之前生成的 `KtProviderInitializer` 的实现类
会自动调用 `KtProviderRouter` 实现类

类似于以下代码:
```kotlin
// KtProviderInitializer 实现类
object ModuleKtProviderInitializer : KtProviderInitializer() {
  
  override val router: KtProviderRouter = ModuleKtProviderRouter
  
  override val otherModuleKtProvider: List<KtProviderInitializer> = listOf(
    // 这里根据 gradle 编译时的模块依赖关系生成所依赖模块的 KtProviderInitializer 实现类
    Module1KtProviderInitializer,
    Module2KtProviderInitializer
  )
}
```
```kotlin
// KtProviderRouter 实现类
internal object ModuleKtProviderRouter : KtProviderRouter() {
  override fun initRouter(delegate: IKtProviderDelegate) {
    // 通过 ksp 找到所有注解，然后添加实现类
    delegate.addImplProvider(ITestService::class, "abc") { TestServiceImpl }
    delegate.addImplProvider(ITestService2::class, "123") { TestServiceImpl2 }
  }
}
```


## License
```
Copyright 2023 985892345

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
