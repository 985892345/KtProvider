# KtProvider
![Maven Central](https://img.shields.io/maven-central/v/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=release)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.985892345/provider-api?server=https://s01.oss.sonatype.org&label=SNAPSHOT)

[中文文档](README_CN.md)

Cross-Module Service Provisioning Framework with KMP Support
- Multi-module Project with KMP Support
- Suitable for use in Compose Multiplatform environment.

## Setup
Across all modules:
<details open>
<summary>Kotlin Multiplatform projects</summary>

```kotlin
// build.gradle.kts
plugins {
  id("com.google.devtools.ksp")
  id("io.github.985892345.KtProvider") version "x.y.z"
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // The provider-manager dependency can be optionally added. 
      // Additionally, you have the option to implement your own provider-manager.
      implementation(ktProvider.manager)
    }
  }
}

dependencies {
  // Please refer to the official documentation for KSP configurations: 
  // https://kotlinlang.org/docs/ksp-multiplatform.html
  add("kspCommonMainMetadata", ktProvider.ksp)
  add("kspAndroid", ktProvider.ksp)
  add("kspDesktop", ktProvider.ksp)
  add("kspIosX64", ktProvider.ksp)
  add("kspIosArm64", ktProvider.ksp)
  add("kspIosSimulatorArm64", ktProvider.ksp)
  // ...
  
  // The provider-api dependency is already included with the Gradle plugin.
}
```
</details>

<details>
<summary>Only Jvm or Android projects</summary>

```kotlin
// build.gradle.kts
plugins {
  id("com.google.devtools.ksp")
  id("io.github.985892345.KtProvider") version "x.y.z"
}

dependencies {
  // The provider-manager dependency can be optionally added. 
  // Additionally, you have the option to implement your own provider-manager.
  implementation(ktProvider.manager)
  // ksp
  ksp(ktProvider.ksp)
}
```
</details>

## Initialization
Kotlin/Jvm: It is recommended to perform initialization in the `main` function.
```kotlin
fun main() {
  // Invoke the automatically generated XXXKtProviderInitializer class (module name + KtProviderInitializer)
  // This class will be automatically generated during the build process. 
  // Alternatively, you can directly invoke the generateXXXKtProviderInitializerImpl Gradle task to generate it.
  XXXKtProviderInitializer.tryInitKtProvider()
}
```

Android: It is recommended to perform initialization in the `Application#onCreate` method.
```kotlin
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    XXXKtProviderInitializer.tryInitKtProvider()
  }
}
```

iOS: Initialize in `App#init` (Swift) or `application:didFinishLaunchingWithOptions:` (Objective-C)

(I'm not proficient in iOS, so this may not be the most optimal approach).
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

## Usage
Add interface
```kotlin
interface ITestService {
  fun get(): String
}
```

Implement interface
```kotlin
@ImplProvider(clazz = ITestService::class, name = "test")
class TestServiceImpl : ITestService {
  override fun get(): String {
    return "123"
  }
}
```
| Annotation         |                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------|
| **ImplProvider**   | Obtain an instance, which becomes a singleton when the implementation class is an object.          |
| **KClassProvider** | Retrieve the KClass of the implementation class (can be used for Class\<out Activity> in Android). |

Use interface
```kotlin
val service = KtProviderManager.getImplOrNull(ITestService::class, "test")
println(service.get())
```

## Implementation principle
**Automatically generate the implementation class for `KtProviderInitializer`**

The KtProvider Gradle plugin automatically generates the implementation class for `KtProviderInitializer`. 
Then, based on the dependency relationships between modules, 
it automatically invokes the `tryInitKtProvider` method of the implementation classes in other modules.
Therefore, you just need to call the `tryInitKtProvider` method in the startup module to load all routes.

**Please note that this mechanism only allows `implementation` and `api` dependencies on other modules.**


**KSP parses annotations within the module**

Based on KSP, it parses annotations within the module and generates the implementation class for `KtProviderRouter`. 
Then, the previously generated implementation class for `KtProviderInitializer` automatically 
invokes the implementation class of `KtProviderRouter`.

Similar to the following code:
```kotlin
// Implementation class of KtProviderInitializer
object ModuleKtProviderInitializer : KtProviderInitializer() {
  
  override val router: KtProviderRouter = ModuleKtProviderRouter
  
  override val otherModuleKtProvider: List<KtProviderInitializer> = listOf(
    // Here, based on the module dependency relationship during Gradle compilation, 
    // the implementation class of KtProviderInitializer for the dependent modules is generated.
    Module1KtProviderInitializer,
    Module2KtProviderInitializer
  )
}
```
```kotlin
// Implementation class of KtProviderRouter
internal object ModuleKtProviderRouter : KtProviderRouter() {
  override fun initRouter(delegate: IKtProviderDelegate) {
    // By using KSP, find all annotations and then add the implementation classes.
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