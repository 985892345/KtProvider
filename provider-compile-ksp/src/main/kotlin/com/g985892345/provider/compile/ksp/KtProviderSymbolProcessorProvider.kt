package com.g985892345.provider.compile.ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * .
 *
 * @author 985892345
 * 2023/12/4 14:18
 */
@AutoService(SymbolProcessorProvider::class)
class KtProviderSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return KtProviderSymbolProcess(
      environment.logger,
      environment.codeGenerator,
      Options(environment.options),
    )
  }
}