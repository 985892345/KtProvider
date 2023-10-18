package com.g985892345.provider.plugin.kcp.ir.entry

import com.g985892345.provider.plugin.kcp.cache.CacheManagerDir
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * .
 *
 * @author 985892345
 * 2023/10/17 17:19
 */
class KtProviderData(
  val message: MessageCollector,
  val isCheckImpl: Boolean,
  val cacheManagerDir: CacheManagerDir,
) {
}