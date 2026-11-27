package com.iffet.kalkan

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class MainActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())

    private lateinit var daysNum: TextView
    private lateinit var daysSub: TextView
    private lateinit var savesTv: TextView
    private lateinit var bestTv: TextView
    private lateinit var resetBtn: TextView
    private lateinit var a11yRow: View
    private lateinit var adminRow: View
    private lateinit var a11yBtn: Button
    private lateinit var adminBtn: Button
    private lateinit var keyIn: EditText
    private lateinit var modelIn: EditText
    private lateinit var wordsIn: EditText
    private lateinit var allowIn: EditText
    private lateinit var geminiStatus: TextView
    private lateinit var saveBtn: Button
    private lateinit var lockIcon: TextView
    private lateinit var lockTitle: TextView
    private lateinit var lockText: TextView
    private lateinit var finishBtn: Button
    private lateinit var unlockBtn: Button
    private lateinit var relockBtn: Button

    private val admin by lazy { ComponentName(this, AdminReceiver::class.java) }
    private val dpm by lazy { getSystemService(DevicePolicyManager::class.java) }

    private val tick = object : Runnable {
        override fun run() { refresh(); h.postDelayed(this, 1000) }
    }

    private val multiLine = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

    private fun save() {
        Store.saveSettings(this, keyIn.text.toString(), modelIn.text.toString(),
            wordsIn.text.toString(), allowIn.text.toString())
        allowIn.setText(Store.allowText(this))
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Store.migrate(this)
        window.statusBarColor = C.BG_TOP
        window.navigationBarColor = C.BG_BOTTOM

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(28), dp(18), dp(48))
        }

        // --- Üst kısım: başlık ve gün sayacı ---
        col.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(text("☪", 22f, color = C.GOLD).apply { setPadding(0, 0, dp(10), 0) })
            addView(text("İffet Kalkanı", 22f, true))
        })

        col.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(26), dp(18), dp(24))
            background = rounded(C.CARD, 26, 0x55E3C26B)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) }
            daysNum = text("0", 64f, true, C.GOLD).apply {
                gravity = Gravity.CENTER
                setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
            }
            daysSub = text("gün temiz", 16f, color = C.GOLD_SOFT).apply { gravity = Gravity.CENTER }
            savesTv = text("", 14f, color = C.MUTED).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) }
            bestTv = text("", 13f, true, C.GOLD_SOFT).apply { gravity = Gravity.CENTER }
            resetBtn = text("Düştüm, günü sıfırla", 14f, color = C.MUTED).apply {
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(8), dp(16), dp(8))
                background = rounded(0x14FFFFFF, 50, C.LINE)
                layoutParams = LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(14) }
                setOnClickListener { askReset() }
            }
            addView(daysNum); addView(daysSub); addView(savesTv); addView(bestTv); addView(resetBtn)
        })

        // --- Günün ayeti (her gün değişir) ---
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val dv = Content.verses[day % Content.verses.size]
        col.addView(card(soft = true).apply {
            addView(label("Günün ayeti · ${dv.theme}"))
            dv.arabic?.let {
                addView(text(it, 21f, color = C.GOLD).apply {
                    gravity = Gravity.CENTER
                    textDirection = View.TEXT_DIRECTION_RTL
                    setLineSpacing(dp(6).toFloat(), 1.1f)
                    setPadding(0, dp(2), 0, dp(10))
                })
            }
            addView(text("“${dv.meal}”", 16f).apply { setTypeface(Typeface.SERIF, Typeface.ITALIC) })
            addView(text(dv.source, 13f, true, C.GOLD).apply { gravity = Gravity.END })
        })

        // --- Kilit durumu ---
        col.addView(card().apply {
            addView(label("Kilit"))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                lockIcon = text("🔒", 28f).apply { setPadding(0, 0, dp(12), 0) }
                lockTitle = text("", 18f, true)
                addView(lockIcon); addView(lockTitle)
            })
            lockText = text("", 14f, color = C.MUTED); addView(lockText)
            finishBtn = button("Kurulumu bitir ve kilitle", C.GOLD, C.BG_BOTTOM) {
                save()
                Store.finishSetup(this@MainActivity)
                Toast.makeText(this@MainActivity, "Kilitlendi. Bismillah, yolun açık olsun.", Toast.LENGTH_LONG).show()
            }.apply { setTypeface(typeface, Typeface.BOLD) }
            unlockBtn = button("Değişiklik yapmak istiyorum (10 dk bekle)", C.GREY) {
                Store.requestUnlock(this@MainActivity)
            }
            relockBtn = button("Vazgeçtim, hemen kilitle", C.GREEN) { Store.relock(this@MainActivity) }
            addView(finishBtn); addView(unlockBtn); addView(relockBtn)
        })

        // --- Koruma izinleri ---
        col.addView(card().apply {
            addView(label("Koruma"))
            a11yRow = statusRow("Erişilebilirlik izni"); addView(a11yRow)
            adminRow = statusRow("Silinme koruması"); addView(adminRow)
            a11yBtn = button("Erişilebilirlik iznini aç") {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            adminBtn = button("Silinme korumasını aç") {
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                    .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Bunu açınca İffet Kalkanı dürtü anında silinemez."))
            }
            addView(a11yBtn); addView(adminBtn)
            addView(text("Telegram, X (Twitter) ve gizli sekme her zaman engelli.", 13f, color = C.MUTED).apply {
                setPadding(0, dp(10), 0, 0)
            })
        })

        // --- Gemini ---
        col.addView(card().apply {
            addView(label("Gemini site kontrolü"))
            addView(text("aistudio.google.com'dan ücretsiz anahtar al. Her satıra bir anahtar yaz; biri dolunca sıradakine geçer. Sadece site adı gönderilir.", 13f, color = C.MUTED))
            keyIn = input("Her satıra bir API anahtarı", Store.apiKeyText(this@MainActivity), 2, multiLine)
            addView(keyIn)
            addView(text("Modeller (yukarıdan aşağı denenir)", 13f, true, C.GOLD_SOFT).apply { setPadding(0, dp(12), 0, 0) })
            modelIn = input(Store.DEFAULT_MODELS.joinToString("\n"), Store.modelText(this@MainActivity), 3, multiLine)
            addView(modelIn)
            geminiStatus = text("", 13f, color = C.MUTED).apply { setPadding(0, dp(8), 0, 0) }
            addView(geminiStatus)
        })

        // --- Kelimeler ve izinli siteler ---
        col.addView(card().apply {
            addView(label("Ekstra yasaklı kelimeler"))
            wordsIn = input("ör: kelime1, kelime2", Store.extraWords(this@MainActivity), 1,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            addView(wordsIn)
            addView(label("İzin verilen siteler").apply { setPadding(0, dp(18), 0, dp(6)) })
            addView(text("Yanlışlıkla engellenen siteleri yaz (ör. vapi.ai). Klavye koruması orada da çalışır.", 13f, color = C.MUTED))
            allowIn = input("ör: vapi.ai", Store.allowText(this@MainActivity), 2, multiLine)
            addView(allowIn)
            saveBtn = button("Ayarları kaydet") {
                save()
                Toast.makeText(this@MainActivity, "Kaydedildi", Toast.LENGTH_SHORT).show()
            }
            addView(saveBtn)
        })

        setContentView(ScrollView(this).apply {
            background = gradientBg()
            addView(col)
        })
    }

    private fun askReset() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("Günü sıfırla")
            .setMessage("Dürüst olman zaten kazanmanın başlangıcı. Sayaç sıfırlanacak, en uzun serin saklanacak.\n\nSıfırlansın mı?")
            .setPositiveButton("Sıfırla, yeniden başlıyorum") { _, _ ->
                Store.resetDays(this)
                refresh()
                AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("Bismillah, yeni bir başlangıç")
                    .setMessage("“Ey nefislerine karşı aşırı giden kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Allah bütün günahları bağışlar.”\n— Zümer Suresi, 53\n\n“Ancak tövbe eden, iman edip salih amel işleyenler başka. Allah onların kötülüklerini iyiliklere çevirir.”\n— Furkan Suresi, 70\n\nKalk, abdest al, tövbe et. Düşmek değil, düştüğün yerde kalmak kaybettirir.")
                    .setPositiveButton("Amin, devam") { d, _ -> d.dismiss() }
                    .show()
            }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    override fun onResume() { super.onResume(); TrustedClock.syncFromNetwork(this); h.post(tick) }
    override fun onPause() { h.removeCallbacks(tick); super.onPause() }

    private fun a11yOn(): Boolean =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("$packageName/") == true

    private fun refresh() {
        val a11y = a11yOn()
        val adm = dpm.isAdminActive(admin)
        val setup = Store.setupDone(this)
        val unlocked = Store.isUnlocked(this)

        a11yRow.setChip(a11y)
        adminRow.setChip(adm)
        a11yBtn.visibility = if (a11y) View.GONE else View.VISIBLE
        adminBtn.visibility = if (adm) View.GONE else View.VISIBLE

        val saves = Store.saves(this)
        savesTv.text = if (saves == 0) "Kalkan hazır. Allah yardımcın olsun."
            else "Kalkan seni $saves kez korudu"

        geminiStatus.text = "Anahtar: ${Store.apiKeys(this).size} · Model: ${Store.models(this).size}\n${GeminiChecker.lastStatus}"

        listOf(keyIn, modelIn, wordsIn, allowIn, saveBtn).forEach {
            it.isEnabled = unlocked
            it.alpha = if (unlocked) 1f else 0.45f
        }

        if (!setup) {
            daysNum.text = "—"
            daysSub.text = "kurulum bekleniyor"
            bestTv.text = ""
            resetBtn.visibility = View.GONE
            lockIcon.text = "🛠️"
            lockTitle.text = "Kurulum"
            lockText.text = "İki izni aç, ayarları yap ve kilitle. Kilitledikten sonra her değişiklik için 10 dk beklemen gerekecek."
            finishBtn.visibility = View.VISIBLE
            finishBtn.isEnabled = a11y && adm
            finishBtn.alpha = if (a11y && adm) 1f else 0.45f
            unlockBtn.visibility = View.GONE
            relockBtn.visibility = View.GONE
            return
        }

        val d = Store.currentDays(this)
        daysNum.text = d.toString()
        val best = Store.bestDays(this)
        bestTv.text = if (best > 0) "En uzun serin: $best gün" else ""
        daysSub.text = "gün temiz"
        resetBtn.visibility = View.VISIBLE
        finishBtn.visibility = View.GONE

        val pending = Store.pendingMsLeft(this)
        val open = Store.openMsLeft(this)
        when {
            pending > 0 -> {
                lockIcon.text = "⏳"; lockTitle.text = "Bekleme: ${fmt(pending)}"
                lockText.text = "Bitince 10 dk boyunca ayarları değiştirebilirsin. Bu sürede dürtü geçerse vazgeç."
            }
            open > 0 -> {
                lockIcon.text = "🔓"; lockTitle.text = "Açık: ${fmt(open)}"
                lockText.text = "Süre bitince kendiliğinden kilitlenir."
            }
            else -> {
                lockIcon.text = "🔒"; lockTitle.text = "Kilitli"
                lockText.text = "Ayarlar, erişilebilirlik ve silme ekranları kapalı."
            }
        }
        val busy = pending > 0 || open > 0
        unlockBtn.visibility = if (busy) View.GONE else View.VISIBLE
        relockBtn.visibility = if (busy) View.VISIBLE else View.GONE
    }
}
