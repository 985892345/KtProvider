package com.g985892345.provider.plugin.kcp.ir.entry

import com.g985892345.provider.plugin.kcp.KtProviderGradlePlugin
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 23:05
 */
@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class KtProviderRegistrar : CompilerPluginRegistrar() {
  override val supportsK2: Boolean
    get() = true
  
  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val message = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    val isCheckImpl = configuration.get(KtProviderGradlePlugin.ARG_IS_CHECK_IMPL) ?: true
    IrGenerationExtension.registerExtension(KtProviderExtension(message, isCheckImpl))
  }
}