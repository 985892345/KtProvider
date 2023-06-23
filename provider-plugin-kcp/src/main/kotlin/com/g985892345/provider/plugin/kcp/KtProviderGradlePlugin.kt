package com.g985892345.provider.plugin.kcp

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 23:04
 */
@AutoService(CommandLineProcessor::class)
@OptIn(ExperimentalCompilerApi::class)
class KtProviderGradlePlugin : CommandLineProcessor {
  companion object {
    private const val OPTION_PACKAGE = "package"
    
    val ARG_PACKAGE = CompilerConfigurationKey<List<String>>(OPTION_PACKAGE)
  }
  override val pluginId: String = BuildConfig.PLUGIN_ID
  override val pluginOptions: Collection<AbstractCliOption> = listOf(
    CliOption(
      optionName = OPTION_PACKAGE,
      valueDescription = "string",
      description = "类名全称，形式如下: a.b&a.c",
      required = false
    )
  )
  
  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (option.optionName) {
      OPTION_PACKAGE -> configuration.put(ARG_PACKAGE, value.split("&"))
    }
  }
}