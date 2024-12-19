package com.ktprovider.sample.module

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.IHelloWorldService
import com.g985892345.provider.manager.KtProvider
import commonMain

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 15:04
 */
class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    commonMain()
    setContentView(
      TextView(this).apply {
        layoutParams = FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )
        text = KtProvider.impl(IHelloWorldService::class).get()
        gravity = Gravity.CENTER
      }
    )
  }
}