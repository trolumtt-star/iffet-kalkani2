package com.iffet.kalkan

import android.accessibilityservice.AccessibilityService
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/**
 * +18 bir şey yakalanınca ekranın TAMAMINI 2 dakika kaplayan ayet ekranı.
 * Erişilebilirlik servisi penceresi olduğu için geri/ana ekran tuşlarıyla kapanmaz, her uygulamanın üstünde durur.
 * Ayetler 15 saniyede bir değişir. Telefon çalarsa arama ekranı için geçici olarak gizlenir.
 */
class ShieldOverlay(private val svc: AccessibilityService) {
    companion object {
        const val DURATION_MS = 2 * 60_000L
        private const val VERSE_MS = 15_000L
    }

    private val wm = svc.getSystemService(WindowManager::class.java)
    private val h = Handler(Looper.getMainLooper())
    private var root: View? = null
    private var endAt = 0L
    private var verses: List<Verse> = emptyList()
    private var idx = 0

    private lateinit var themeTv: TextView
    private lateinit var arabicTv: TextView
    private lateinit var mealTv: TextView
    private lateinit var sourceTv: TextView
    private lateinit var verseBox: LinearLayout
    private lateinit var timerTv: TextView
    private lateinit var bar: ProgressBar
    private lateinit var reasonTv: TextView
    private lateinit var countTv: TextView

    val isShowing get() = root != null

    fun show(reason: String) {
        val saves = Store.addSave(svc)
        if (root != null) { // zaten açıksa süreyi baştan başlat
            endAt = TrustedClock.now(svc) + DURATION_MS
            reasonTv.text = reason
            countTv.text = savesText(saves)
            return
        }
        endAt = TrustedClock.now(svc) + DURATION_MS
        verses = Content.shuffledSequence()
        idx = 0

        val view = build(reason, saves)
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.OPAQUE
        ).apply { gravity = Gravity.TOP or Gravity.START }

        try {
            wm.addView(view, lp)
            root = view
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(400).start()
            showVerse(animate = false)
            h.post(tick)
            h.postDelayed(nextVerse, VERSE_MS)
        } catch (_: Exception) {
            root = null
        }
    }

    /** Telefon çalarken arama ekranı görünsün diye. */
    fun setHiddenForCall(hidden: Boolean) {
        root?.visibility = if (hidden) View.GONE else View.VISIBLE
    }

    fun hide() {
        h.removeCallbacks(tick)
        h.removeCallbacks(nextVerse)
        val v = root ?: return
        root = null
        v.animate().alpha(0f).setDuration(350).withEndAction {
            try { wm.removeView(v) } catch (_: Exception) {}
        }.start()
    }

    private val tick = object : Runnable {
        override fun run() {
            val left = endAt - TrustedClock.now(svc)
            if (left <= 0) { hide(); return }
            timerTv.text = fmt(left)
            bar.progress = (1000 * (DURATION_MS - left) / DURATION_MS).toInt()
            h.postDelayed(this, 250)
        }
    }

    private val nextVerse = object : Runnable {
        override fun run() {
            idx = (idx + 1) % verses.size
            showVerse(animate = true)
            h.postDelayed(this, VERSE_MS)
        }
    }

    private fun showVerse(animate: Boolean) {
        val v = verses[idx]
        val apply = {
            themeTv.text = v.theme
            arabicTv.visibility = if (v.arabic == null) View.GONE else View.VISIBLE
            arabicTv.text = v.arabic ?: ""
            mealTv.text = "“${v.meal}”"
            sourceTv.text = v.source
        }
        if (!animate) { apply(); return }
        verseBox.animate().alpha(0f).translationY(-svc.dp(10).toFloat()).setDuration(450).withEndAction {
            apply()
            verseBox.translationY = svc.dp(10).toFloat()
            verseBox.animate().alpha(1f).translationY(0f).setDuration(550).start()
        }.start()
    }

    private fun savesText(n: Int) = "Kalkan seni bugüne kadar $n kez korudu. Bu sefer de kazan."

    private fun build(reason: String, saves: Int): View = with(svc) {
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(52), dp(22), dp(36))
        }

        col.addView(text("☪", 30f, color = C.GOLD).apply { gravity = Gravity.CENTER })
        col.addView(text("Dur kardeşim", 30f, true).apply { gravity = Gravity.CENTER })
        reasonTv = text(reason, 15f, color = C.GOLD_SOFT).apply { gravity = Gravity.CENTER }
        col.addView(reasonTv)

        // Ayet kartı
        verseBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
            background = rounded(C.CARD_SOFT, 24, 0x55E3C26B)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22) }
        }
        themeTv = label("")
        arabicTv = text("", 25f, color = C.GOLD).apply {
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
            setLineSpacing(dp(8).toFloat(), 1.1f)
            setPadding(0, dp(6), 0, dp(14))
        }
        mealTv = text("", 19f).apply {
            setTypeface(Typeface.SERIF, Typeface.ITALIC)
            setLineSpacing(0f, 1.25f)
        }
        sourceTv = text("", 14f, true, C.GOLD).apply { gravity = Gravity.END; setPadding(0, dp(10), 0, 0) }
        verseBox.addView(themeTv)
        verseBox.addView(arabicTv)
        verseBox.addView(View(this).apply {
            setBackgroundColor(C.LINE)
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { bottomMargin = dp(12) }
        })
        verseBox.addView(mealTv)
        verseBox.addView(sourceTv)
        col.addView(verseBox)

        // Öneri
        col.addView(card(soft = true).apply {
            addView(label("Şimdi ne yap"))
            addView(text(Content.tips.random(), 16f))
        })

        countTv = text(savesText(saves), 14f, color = C.MUTED).apply {
            gravity = Gravity.CENTER; setPadding(0, dp(18), 0, 0)
        }
        col.addView(countTv)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(col)
        }

        // Alt kısım: geri sayım ve ilerleme çubuğu (hep görünür)
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(14), dp(22), dp(30))
            setBackgroundColor(0xE606140F.toInt())
        }
        timerTv = text(fmt(DURATION_MS), 34f, true, C.GOLD).apply { gravity = Gravity.CENTER }
        bottom.addView(timerTv)
        bottom.addView(text("Bu ekran süre bitince kendiliğinden kapanır. Bu sürede zikret, dürtü geçecek.", 13f, color = C.MUTED)
            .apply { gravity = Gravity.CENTER })
        bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 1000
            progressTintList = ColorStateList.valueOf(C.GOLD)
            progressBackgroundTintList = ColorStateList.valueOf(0x33FFFFFF)
            layoutParams = LinearLayout.LayoutParams(-1, dp(6)).apply { topMargin = dp(10) }
        }
        bottom.addView(bar)

        FrameLayout(this).apply {
            background = gradientBg()
            isClickable = true // dokunuşlar alttaki uygulamaya geçmesin
            addView(scroll, FrameLayout.LayoutParams(-1, -1).apply { bottomMargin = dp(150) })
            addView(bottom, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        }
    }
}
