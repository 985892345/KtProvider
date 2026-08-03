package com.g985892345.provider.compile.ksp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OptionsTest {

  @Test
  fun parsesRequiredValuesAndUsesOptionalDefaults() {
    val options = Options(requiredOptions())

    assertEquals("com.example.router", options.packageName)
    assertEquals("ExampleRouter", options.className)
    assertEquals("com.example.initializer", options.initializerPackageName)
    assertEquals("ExampleInitializer", options.initializerClassName)
    assertFalse(options.logEnable)
    assertEquals(emptyList(), options.dependModuleProjects)
    assertEquals(5, options.processMaxCount)
  }

  @Test
  fun parsesDependencyListAndCustomProcessingOptions() {
    val options = Options(
      requiredOptions() + mapOf(
        "ktProviderLogEnable" to "true",
        "ktProviderDependModuleProjects" to
            "com.example.ApiInitializer + com.example.ImplInitializer",
        "ktProviderProcessMaxCount" to "8",
      )
    )

    assertEquals(
      listOf("com.example.ApiInitializer", "com.example.ImplInitializer"),
      options.dependModuleProjects,
    )
    assertEquals(8, options.processMaxCount)
    assertEquals(true, options.logEnable)
  }

  private fun requiredOptions() = mapOf(
    "ktProviderRouterPackageName" to "com.example.router",
    "ktProviderRouterClassName" to "ExampleRouter",
    "ktProviderInitializerPackageName" to "com.example.initializer",
    "ktProviderInitializerClassName" to "ExampleInitializer",
  )
}
