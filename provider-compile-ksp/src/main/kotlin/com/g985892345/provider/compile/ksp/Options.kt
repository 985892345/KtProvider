package com.g985892345.provider.compile.ksp

/**
 * .
 *
 * @author 985892345
 * 2023/12/4 21:29
 */
class Options(
  val packageName: String,
  val className: String,
  val initializerPackageName: String,
  val initializerClassName: String,
  val logEnable: Boolean,
  val maxProcessTimes: Int,
) {
  constructor(options: Map<String, String>) : this(
    options["ktProviderRouterPackageName"]!!, // It assigned by the Gradle plugin
    options["ktProviderRouterClassName"]!!, // It assigned by the Gradle plugin
    options["ktProviderInitializerPackageName"]!!, // It assigned by the Gradle plugin
    options["ktProviderInitializerClassName"]!!, // It assigned by the Gradle plugin
    options["ktProviderLogEnable"]?.toBooleanStrictOrNull() ?: false,
    options["ktProviderMaxProcessTimes"]?.toIntOrNull()?.coerceAtLeast(1) ?: 3,
  )
}