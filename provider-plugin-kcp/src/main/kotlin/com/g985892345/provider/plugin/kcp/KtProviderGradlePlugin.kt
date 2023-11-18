package com.g985892345.provider.plugin.kcp

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

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
    private const val OPTION_CACHE_PATH = "cachePath"
    private const val OPTION_INITIALIZER_CLASS = "initializerClass"
    
    val ARG_IS_CHECK_IMPL = CompilerConfigurationKey<Boolean>(OPTION_IS_CHECK_IMPL)
    val ARG_CACHE_PATH = CompilerConfigurationKey<File>(OPTION_CACHE_PATH)
    val ARG_INITIALIZER_CLASS = CompilerConfigurationKey<String>(OPTION_INITIALIZER_CLASS)
  }
  override val pluginId: String = BuildConfig.PLUGIN_ID
  override val pluginOptions: Collection<AbstractCliOption> = listOf(
    CliOption(
      optionName = OPTION_IS_CHECK_IMPL,
      valueDescription = "boolean",
      description = "是否检查被注解类是注解中标注参数的实现类(默认开启)",
      required = false
    ),
    CliOption(
      optionName = OPTION_CACHE_PATH,
      valueDescription = "String",
      description = "缓存目录的路径，用于保存开启增量编译后需要编译信息",
      required = true
    ),
    CliOption(
      optionName = OPTION_INITIALIZER_CLASS,
      valueDescription = "String",
      description = "KtProviderInitializer 的实现类全称，用于 ir 插桩",
      required = true
    ),
  )
  
  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (option.optionName) {
      OPTION_IS_CHECK_IMPL -> configuration.put(ARG_IS_CHECK_IMPL, value.toBooleanStrictOrNull() ?: true)
      OPTION_CACHE_PATH -> configuration.put(ARG_CACHE_PATH, File(value))
      OPTION_INITIALIZER_CLASS -> configuration.put(ARG_INITIALIZER_CLASS, value)
    }
  }
}