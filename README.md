# KtProvider
![Maven Central](https://img.shields.io/maven-central/v/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=release)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=SNAPSHOT)

支持 KMP 的跨模块服务提供轻量级框架  
- 支持 KMP，可用于 Compose Multiplatform 中 (目前未测试，理论上支持)
- 支持 KMP 的多模块工程 (jvm 和 Android 项目已通过测试)
- 只提供底层支持，允许对服务管理者进一步封装

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

kotlin {
  sourceSets {
    commonMain.dependencies {
      // provider-api 依赖
      implementation("io.github.985892345:provider-api:x.y.z")
    }
  }
}

dependencies {
  // ksp 相关配置请参考官方文档: https://kotlinlang.org/docs/ksp-multiplatform.html
  val ktProviderKsp = "io.github.985892345:provider-compile-ksp:x.y.z"
  add("kspCommonMainMetadata", ktProviderKsp)
  add("kspAndroid", ktProviderKsp)
  add("kspDesktop", ktProviderKsp)
  add("kspIosX64", ktProviderKsp)
  add("kspIosArm64", ktProviderKsp)
  add("kspIosSimulatorArm64", ktProviderKsp)
  // ...
  
  // provider-init 和 provider-annotation 依赖已随 gradle 插件一起添加
  // // provider-manager 可选择性添加，详细请看后文
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

kotlin {
  sourceSets {
    commonMain.dependencies {
      // provider-api 依赖
      implementation("io.github.985892345:provider-api:x.y.z")
    }
  }
}

dependencies {
  // ksp 相关配置请参考官方文档: https://kotlinlang.org/docs/ksp-multiplatform.html
  val ktProviderKsp = "io.github.985892345:provider-compile-ksp:x.y.z"
  add("kspCommonMainMetadata", ktProviderKsp)
  add("kspAndroid", ktProviderKsp)
  add("kspDesktop", ktProviderKsp)
  add("kspIosX64", ktProviderKsp)
  add("kspIosArm64", ktProviderKsp)
  add("kspIosSimulatorArm64", ktProviderKsp)
  // ...
  
  // provider-init 和 provider-annotation 依赖已随 gradle 插件一起添加
  // provider-manager 可选择性添加，详细请看后文
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
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("io.github.985892345:provider-manager:x.y.z")
    }
  }
}
```
#### 代码中
```kotlin
// 使用 KtProviderManager 获取服务
val service = KtProviderManager.getImplOrNull(ITestService::class, "test")
println(service.get())
```

## 实现原理
### 自动生成 KtProviderInitializer 实现类
KtProvider 的 gradle 插件会自动生成 `KtProviderInitializer` 的实现类，
然后根据模块之间的依赖关系，自动调用其他模块实现类的 `tryInitKtProvider()` 方法
**（但只允许 implementation、api 依赖其他模块）**  
所以只需要在启动模块中调用 `tryInitKtProvider()` 方法即可加载全部路由

### KSP 解析模块内注解
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
    delegate.addImplProvider(ITestService::class, "zzz") { Test1 }
    delegate.addImplProvider(ITestService2::class, "zzz") { Test2 }
  }
}
```


## 自定义封装
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
