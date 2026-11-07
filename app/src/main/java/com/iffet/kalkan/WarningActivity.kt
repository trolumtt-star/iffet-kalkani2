package com.iffet.kalkan

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class WarningActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private var countdown: TextView? = null
    private var until = 0L
    private var app = ""

    private val tick = object : Runnable {
        override fun run() {
            val left = until - System.currentTimeMillis()
            countdown?.text = when {
                until == 0L -> ""
                left > 0 -> "⏳ $app ${fmt(left)} sonra açılacak."
                else -> "$app artık açılabilir. Ama gerçekten girmek istiyor musun?"
            }
            if (left > 0) h.postDelayed(this, 1000)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        show(intent)
    }

    override fun onNewIntent(i: Intent) {
        super.onNewIntent(i)
        setIntent(i)
        show(i)
    }

    private fun show(i: Intent) {
        h.removeCallbacks(tick)
        until = i.getLongExtra("until", 0L)
        app = i.getStringExtra("app") ?: ""
        val reason = i.getStringExtra("reason") ?: ""
        val (verse, source) = Content.random()
        val white = 0xFFFFFFFF.toInt()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(56), dp(24), dp(32))
        }
        col.addView(text("🛑 Dur kardeşim", 30f, true, white).apply { gravity = Gravity.CENTER })
        col.addView(text(reason, 17f, color = 0xFFFFD7A0.toInt()).apply { gravity = Gravity.CENTER })

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable().apply { setColor(0x33FFFFFF); cornerRadius = dp(16).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24); bottomMargin = dp(16) }
        }
        card.addView(text("“$verse”", 21f, color = white).apply { setTypeface(typeface, Typeface.ITALIC) })
        card.addView(text("— $source", 15f, color = 0xFFB8E6CF.toInt()).apply { gravity = Gravity.END })
        col.addView(card)

        col.addView(text("💡 " + Content.tips.random(), 17f, color = white))
        countdown = text("", 17f, true, 0xFFFFD7A0.toInt()).apply { gravity = Gravity.CENTER }
        col.addView(countdown)
        col.addView(button("Tamam, vazgeçtim. Ana ekrana dön", 0xFF2E8B57.toInt()) { goHome() })

        setContentView(ScrollView(this).apply {
            setBackgroundColor(0xFF0F3D2E.toInt())
            addView(col)
        })
        h.post(tick)
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    @Deprecated("Geri tuşu yasaklı uygulamaya değil ana ekrana götürür")
    override fun onBackPressed() = goHome()

    override fun onDestroy() {
        h.removeCallbacks(tick)
        super.onDestroy()
    }
}
