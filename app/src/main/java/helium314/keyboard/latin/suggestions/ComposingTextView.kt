/*
 * Copyright (C) 2023 The HeliBoard Project
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.suggestions

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.settings.Settings

class ComposingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.composing_text_view, this, true)
        textView = findViewById(R.id.composing_text)

        // Apply keyboard colors and border
        val colors = Settings.getValues().mColors
        background = ContextCompat.getDrawable(context, R.drawable.composing_text_border)
        (background as? GradientDrawable)?.setColor(colors.get(ColorType.MAIN_BACKGROUND))
        textView.setTextColor(colors.get(ColorType.KEY_TEXT))
    }

    fun setComposingText(text: String) {
        textView.text = text
    }

    fun setVisibilityForInputType(inputType: Int) {
        // Show for all except numeric and password types
        val show = when (inputType and android.text.InputType.TYPE_MASK_CLASS) {
            android.text.InputType.TYPE_CLASS_NUMBER,
            android.text.InputType.TYPE_CLASS_PHONE -> false
            else -> {
                val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
                variation != android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD &&
                variation != android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD &&
                variation != android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            }
        }
        visibility = if (show) VISIBLE else GONE
    }
}
