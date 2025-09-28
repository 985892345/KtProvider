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
  val dependModuleProjects: List<String>,
  val processMaxCount: Int,
) {
  constructor(options: Map<String, String>) : this(
    packageName = options["ktProviderRouterPackageName"]!!, // It assigned by the Gradle plugin
    className = options["ktProviderRouterClassName"]!!, // It assigned by the Gradle plugin
    initializerPackageName = options["ktProviderInitializerPackageName"]!!, // It assigned by the Gradle plugin
    initializerClassName = options["ktProviderInitializerClassName"]!!, // It assigned by the Gradle plugin
    logEnable = options["ktProviderLogEnable"]?.toBooleanStrictOrNull() ?: false,
    dependModuleProjects = options["ktProviderDependModuleProjects"]?.split(" + ") ?: emptyList(),
    processMaxCount = options["ktProviderProcessMaxCount"]?.toInt() ?: 5,
  )
}