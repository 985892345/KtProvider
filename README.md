# KtProvider
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.985892345/provider-init?server=https://s01.oss.sonatype.org&label=KtProvider-SNAPSHOT)  
支持 KMP(KMM) 的跨模块服务提供轻量级框架  
- 支持 KMP(KMM)，可用于 Compose Multiplatform 中 (目前未测试，理论上支持)
- 支持 KMP(KMM) 的多模块工程 (jvm 和 Android 项目已通过测试)
- 只提供底层支持，允许对服务管理者进一步封装
- 支持增量编译

## 引入教程
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

### 启动模块
如果你是 Kotlin/Jvm 项目，启动模块一般是 `main` 函数所在模块，
如果是 Android 项目，启动模式是引入了 `com.android.application` 插件的模块   
可查看 [sample](sample) 使用示例
#### build.gradle.kts
```kotlin
plugins {
  id("io.github.985892345.KtProvider") version "x.y.z"
}

ktProvider {
  // 可以设置一些东西
}
```
使用 KtProvider 插件后会自动生成一个 `KtProviderInitializer` 的实现类，
并且会根据该模块的依赖关系自动调用其他模块的 `tryInitKtProvider()` 方法

#### 代码中
```kotlin
// 然后在启动函数中进行加载
fun main() {
  // 调用自动生成的 XXXKtProviderInitializer
  // 该类名默认为 模块名+KtProviderInitializer，可在 ktProvider 闭包中设置
  XXXKtProviderInitializer.tryInitKtProvider()
}

// Android 在 Application 的 onCreate 中加载
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    XXXKtProviderInitializer.tryInitKtProvider()
  }
}
```

### api 模块
api 模块即定义接口的模块，此模块只需要定义接口即可，不需要引入 KtProvider 插件
```kotlin
interface ITestService {
  fun get(): String
}
```

### 实现模块
实现模块依赖 api 模块，启动模块需要间接或直接依赖实现模块（只支持 implementation 和 api）
#### build.gradle.kts
```kotlin
plugins {
  id("io.github.985892345.KtProvider") version "x.y.z"
}

ktProvider {
  // 可以设置一些东西，比如设置 KtProviderInitializer 实现类的代理类
}
```
#### 代码中
```kotlin
// 打上注解
@ImplProvider(clazz = ITestService::class, name = "test") // class 与 name 必须包含一个
class TestServiceImpl : ITestService {
  override fun get(): String {
    return "123"
  }
}
```
| 注解              | 作用            |                                            |
|-----------------|---------------|--------------------------------------------|
| ImplProvider    | 每次获取都是新的实例    |                                            |
| KClassProvider  | 获取实现类的 KClass | 封装一下就可用于获取 Android 中的 Class\<out Activity> |



### 服务消费模块
#### build.gradle.kts
```kotlin
// 引入 provider-manager 依赖
// 该依赖并不是必选项，你可以实现自己的服务管理者
dependencies {
  val krProviderVersion = "x.y.z"
  // 如果你是 KMM 项目
  implementation("io.github.985892345:provider-manager:$krProviderVersion")
  
  // 如果你只是 Kotlin/Jvm 项目（比如 Android 项目），只需要依赖 -jvm 即可
  implementation("io.github.985892345:provider-manager-jvm:$krProviderVersion")
  
  // 当然也有 -js 和 -native（但未经过测试）
}
```
#### 代码中
```kotlin
// 使用 KtProviderManager 的获取服务
val service = KtProviderManager.getImplOrNull(ITestService::class, "test")
println(service.get())
```

## 实现原理
### ir 查桩
基于 Kotlin Compile Plugin 中的 ir 插桩，寻找启动模块依赖的所有模块中包含有对应注解的类，
然后添加到 `KtProviderInitializer` 实现类的 `initAddAllProvider` 方法下  
类似于如下代码:
```kotlin
object KtProvider : KtProviderInitializer() {
  // 如果没有重写 initAddAllProvider 方法，则会自动重写
  // 如果已经重写，则在方法体的第一行插入 _initImpl()
  override fun initAddAllProvider() {
    _initImpl() // 先调用 _initImpl 方法进行初始化
    // ... 后面是你重写的内容
  }
  
  // 该方法由 ir 添加，用于收集当前模块的所有路由
  private fun _initImpl() {
    addKClassProvider("key1") { TestClass1::class }
    addNewImplProvider("key2") { TestClass2() }
    addSingleImplProvider("key3") { TestClass3() }
    // ...
  }
}
```
### 自动生成 KtProviderInitializer 实现类
KtProvider 的 gradle 插件会自动生成 `KtProviderInitializer` 的实现类，
然后根据模块之间的依赖关系，自动调用其他模块实现类的 `tryInitKtProvider()` 方法
（但只允许 implementation、api 依赖其他模块）  
所以只需要在启动模块中调用 `tryInitKtProvider()` 方法即可加载全部路由


## 自定义封装
  

`io.github.985892345.KtProvider` 的 gradle 插件默认依赖了 `provider-init`、`provider-annotation`，
你可以不依赖 `provider-manager`，我只设计了服务提供框架的底层支持，你可以实现自己的 `KtProviderManager` 来扩展其他功能，具体实现逻辑请看源码


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
