package com.iffet.kalkan

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** XML layout yazmamak için küçük yardımcılar. */
fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

fun Context.text(s: String, size: Float = 16f, bold: Boolean = false, color: Int = 0xFF1B1B1B.toInt()) =
    TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(6), 0, dp(6))
    }

fun Context.button(s: String, bg: Int = 0xFF1E6B4F.toInt(), onClick: () -> Unit) =
    Button(this).apply {
        text = s
        isAllCaps = false
        textSize = 16f
        setTextColor(0xFFFFFFFF.toInt())
        background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(12).toFloat() }
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(10) }
    }

fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}
