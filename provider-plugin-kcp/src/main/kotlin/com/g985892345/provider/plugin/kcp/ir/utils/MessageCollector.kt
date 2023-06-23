package com.g985892345.provider.plugin.kcp.ir.utils

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * .
 *
 * @author 985892345
 * 2023/6/22 19:42
 */

fun MessageCollector.log(msg: String) {
  report(CompilerMessageSeverity.WARNING, msg)
}