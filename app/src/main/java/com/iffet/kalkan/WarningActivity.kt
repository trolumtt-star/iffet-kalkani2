package com.iffet.kalkan

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Kilitli uygulamaya, Telegram'a veya gizli sekmeye girmeye çalışınca açılan kısa uyarı. */
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
                left > 0 -> "$app ${fmt(left)} sonra açılacak"
                else -> "$app artık açılabilir. Ama gerçekten girmek istiyor musun?"
            }
            countdown?.visibility = if (until == 0L) View.GONE else View.VISIBLE
            if (left > 0) h.postDelayed(this, 1000)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = C.BG_TOP
        window.navigationBarColor = C.BG_BOTTOM
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
        val v = Content.random()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(64), dp(22), dp(36))
        }
        col.addView(text("☪", 30f, color = C.GOLD).apply { gravity = Gravity.CENTER })
        col.addView(text("Dur kardeşim", 30f, true).apply { gravity = Gravity.CENTER })
        col.addView(text(reason, 16f, color = C.GOLD_SOFT).apply { gravity = Gravity.CENTER })

        countdown = text("", 15f, true, C.GOLD).apply {
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = rounded(0x22E3C26B, 50)
            layoutParams = LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(14) }
        }
        col.addView(countdown)

        col.addView(card(soft = true).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24) }
            addView(label(v.theme))
            v.arabic?.let {
                addView(text(it, 23f, color = C.GOLD).apply {
                    gravity = Gravity.CENTER
                    textDirection = View.TEXT_DIRECTION_RTL
                    setLineSpacing(dp(6).toFloat(), 1.1f)
                    setPadding(0, dp(4), 0, dp(12))
                })
            }
            addView(text("“${v.meal}”", 18f).apply { setTypeface(Typeface.SERIF, Typeface.ITALIC) })
            addView(text(v.source, 14f, true, C.GOLD).apply { gravity = Gravity.END })
        })

        col.addView(card(soft = true).apply {
            addView(label("Şimdi ne yap"))
            addView(text(Content.tips.random(), 16f))
        })

        col.addView(button("Tamam, vazgeçtim", C.GOLD, C.BG_BOTTOM) { goHome() }.apply {
            setTypeface(typeface, Typeface.BOLD)
            (layoutParams as LinearLayout.LayoutParams).topMargin = dp(24)
        })

        setContentView(ScrollView(this).apply {
            background = gradientBg()
            isFillViewport = true
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
