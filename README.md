# KtProvider
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.985892345/provider-init?server=https://s01.oss.sonatype.org&label=KtProvider-SNAPSHOT)  
支持 KMM 的跨模块服务提供轻量级框架  
- 支持 KMM，可用于 Compose Multiplatform 中 (目前未测试，理论上支持)
- 支持 KMM 的多模块工程 (目前未测试，理论上支持)
- 只提供底层支持，允许对服务管理者进一步封装

## 引入教程
目前还处于测试阶段，未发布稳定包，请先设置 MavenCentral 快照仓库后进行依赖
```kotlin
// setting.gradle.kts

pluginManagement {
  repositories {
    // ...
    // mavenCentral 快照仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}
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

// 引入 provider-init 依赖
dependencies {
  val krProviderVersion = "x.y.z"
  // 如果你是 KMM 项目
  implementation("io.github.985892345:provider-init:$krProviderVersion")
  
  // 如果你只是 Kotlin/Jvm 项目（比如 Android 项目）
  implementation("io.github.985892345:provider-init-jvm:$krProviderVersion")
  
  // 当然也有 -js 和 -native
}

ktProvider {
  packageName {
    // 设置 packageName，则会自动寻找该 package 以及所有子包下的类
    include("com.g985892345.test") // 可设置多个 packageName
  }
}
```
#### 代码中
```kotlin
// 新建一个 kt 类并继承于 ProviderInitialize
object KtProvider : ProviderInitialize()

// 然后在启动函数中进行加载
fun main() {
  KtProvider.init()
}

// Android 在 Application 的 onCreate 中加载
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    KtProvider.init()
  }
}
```

### api 模块
api 模块即定义接口的模块，此模块只需要定义接口即可
```kotlin
interface ITestService {
  fun get(): String
}
```

### 实现模块
实现依赖 api 模块，启动模块需要间接或直接依赖实现模块（使用 runtimeOnly 无效）
#### build.gradle.kts
```kotlin
// 引入 provider-annotation 依赖
dependencies {
  val krProviderVersion = "x.y.z"
  // 如果你是 KMM 项目
  implementation("io.github.985892345:provider-annotation:$krProviderVersion")
  
  // 如果你只是 Kotlin/Jvm 项目（比如 Android 项目）
  implementation("io.github.985892345:provider-annotation-jvm:$krProviderVersion")
  
  // 当然也有 -js 和 -native
}
```
#### 代码中
```kotlin
// 打上注解
@NewImplProvider(clazz = ITestService::class, name = "test") // class 与 name 必须包含一个
class TestServiceImpl : ITestService {
  override fun get(): String {
    return "123"
  }
}
```
| 注解                 | 作用            |                                            |
|--------------------|---------------|--------------------------------------------|
| NewImplProvider    | 每次获取都是新的实例    |                                            |
| SingleImplProvider | 每次获取都是单例      | 依靠 kt 的 lazy 实现，线程安全                       |
| KClassProvider     | 获取实现类的 KClass | 封装一下就可用于获取 Android 中的 Class\<out Activity> |



### 服务消费模块
#### build.gradle.kts
```kotlin
// 引入 provider-manager 依赖
// 该依赖并不是必选项，你可以实现自己服务管理者
dependencies {
  val krProviderVersion = "x.y.z"
  // 如果你是 KMM 项目
  implementation("io.github.985892345:provider-manager:$krProviderVersion")
  
  // 如果你只是 Kotlin/Jvm 项目（比如 Android 项目）
  implementation("io.github.985892345:provider-manager-jvm:$krProviderVersion")
  
  // 当然也有 -js 和 -native
}
```
#### 代码中
```kotlin
// 使用 ProviderManager 的获取服务
val service = ProviderManager.getImplOrNull<ITestService>("test")
println(service.get())
```

## 实现原理
基于 Kotlin Compile Plugin 中的 ir 插桩，寻找启动模块依赖的所有模块中包含有对应注解的类，
然后添加到 `ProviderInitialize` 实现类的 `init` 方法下  
类似于如下代码:
```kotlin
object KtProvider : ProviderInitialize() {
  // 如果没有重写 init 方法，则会自动重写
  // 如果已经重写，则在方法体的第一行插入 _initImpl()
  override fun init() {
    _initImpl() // 先调用 _initImpl 方法进行初始化
    // ... 后面是你重写的内容
  }
  
  private fun _initImpl() {
    addKClassProvider("name1") { TestClass1::class }
    addNewImplProvider("name2") { TestClass2() }
    addSingleImplProvider("name3") { TestClass3() }
    // ...
  }
}
```

## 自定义封装
我只设计了服务提供框架的底层支持，你可以实现自己的 ProviderManager 来扩展其他功能  

`io.github.985892345.KtProvider` 的 gradle 插件只跟 `provider-init`、`provider-annotation` 挂钩，
你可以不依赖 `provider-manager`，实现自己的 ProviderManager，具体实现逻辑请看源码