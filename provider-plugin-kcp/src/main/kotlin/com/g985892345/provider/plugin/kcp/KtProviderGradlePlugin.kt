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
    private const val OPTION_IS_CHECK_IMPL = "isCheckImpl"
    
    val ARG_IS_CHECK_IMPL = CompilerConfigurationKey<Boolean>(OPTION_IS_CHECK_IMPL)
  }
  override val pluginId: String = BuildConfig.PLUGIN_ID
  override val pluginOptions: Collection<AbstractCliOption> = listOf(
    CliOption(
      optionName = OPTION_IS_CHECK_IMPL,
      valueDescription = "boolean",
      description = "是否检查被注解类是注解中标注参数的实现类(默认开启)",
      required = false
    )
  )
  
  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (option.optionName) {
      OPTION_IS_CHECK_IMPL -> configuration.put(ARG_IS_CHECK_IMPL, value.toBooleanStrictOrNull() ?: true)
    }
  }
}