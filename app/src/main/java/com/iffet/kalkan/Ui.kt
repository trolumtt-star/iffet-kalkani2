package com.iffet.kalkan

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** Uygulamanın renkleri: gece yeşili zemin, altın vurgu. */
object C {
    const val BG_TOP = 0xFF0B2A22.toInt()
    const val BG_BOTTOM = 0xFF06140F.toInt()
    const val CARD = 0xFF12382D.toInt()
    const val CARD_SOFT = 0x1FFFFFFF
    const val LINE = 0x33FFFFFF
    const val GOLD = 0xFFE3C26B.toInt()
    const val GOLD_SOFT = 0xFFF3E2B0.toInt()
    const val TEXT = 0xFFF4F1E8.toInt()
    const val MUTED = 0xFFA9BFB6.toInt()
    const val GREEN = 0xFF2FA36B.toInt()
    const val RED = 0xFFC9534F.toInt()
    const val GREY = 0xFF3C5A50.toInt()
}

fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

fun Context.text(s: String, size: Float = 16f, bold: Boolean = false, color: Int = C.TEXT) =
    TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        setLineSpacing(0f, 1.15f)
        if (bold) setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL), Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }

/** Küçük, harfleri açık başlık (bölüm etiketi). */
fun Context.label(s: String) = text(s.uppercase(java.util.Locale("tr", "TR")), 12f, true, C.GOLD).apply {
    letterSpacing = 0.12f
    setPadding(0, dp(2), 0, dp(6))
}

fun Context.rounded(color: Int, radius: Int = 18, stroke: Int = 0) = GradientDrawable().apply {
    setColor(color)
    cornerRadius = dp(radius).toFloat()
    if (stroke != 0) setStroke(dp(1), stroke)
}

fun Context.gradientBg() = GradientDrawable(
    GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(C.BG_TOP, C.BG_BOTTOM)
)

fun Context.card(soft: Boolean = false) = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(dp(18), dp(16), dp(18), dp(18))
    background = rounded(if (soft) C.CARD_SOFT else C.CARD, 20, C.LINE)
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(14) }
}

fun Context.button(s: String, bg: Int = C.GREEN, fg: Int = 0xFFFFFFFF.toInt(), onClick: () -> Unit) =
    Button(this).apply {
        text = s
        isAllCaps = false
        textSize = 16f
        setTextColor(fg)
        stateListAnimator = null
        minHeight = dp(52)
        background = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), rounded(bg, 14), null)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(10) }
    }

fun Context.input(hintText: String, value: String, lines: Int = 1, typeFlags: Int) =
    EditText(this).apply {
        hint = hintText
        setText(value)
        inputType = typeFlags
        minLines = lines
        textSize = 15f
        setTextColor(C.TEXT)
        setHintTextColor(0x66FFFFFF)
        gravity = Gravity.TOP or Gravity.START
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = rounded(0x14FFFFFF, 12, C.LINE)
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(6) }
    }

/** Durum rozeti: "✓ Açık" yeşil, "✕ Kapalı" kırmızı. */
fun Context.statusRow(title: String) = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    setPadding(0, dp(6), 0, dp(6))
    addView(text(title, 15f).apply { layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f) })
    addView(text("", 13f, true).apply {
        tag = "chip"
        setPadding(dp(12), dp(4), dp(12), dp(4))
    })
}

fun View.setChip(on: Boolean) {
    val chip = findViewWithTag<TextView>("chip")
    chip.text = if (on) "✓ Açık" else "✕ Kapalı"
    chip.setTextColor(if (on) 0xFF9FF0C5.toInt() else 0xFFFFB4B0.toInt())
    chip.background = context.rounded(if (on) 0x332FA36B else 0x33C9534F, 50)
}

fun fmt(ms: Long): String {
    val s = (ms + 999) / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}
