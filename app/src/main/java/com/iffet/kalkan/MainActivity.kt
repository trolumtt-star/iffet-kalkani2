package com.iffet.kalkan

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())

    private lateinit var days: TextView
    private lateinit var status: TextView
    private lateinit var a11yBtn: Button
    private lateinit var adminBtn: Button
    private lateinit var keyIn: EditText
    private lateinit var modelIn: EditText
    private lateinit var wordsIn: EditText
    private lateinit var saveBtn: Button
    private lateinit var lockText: TextView
    private lateinit var finishBtn: Button
    private lateinit var unlockBtn: Button
    private lateinit var relockBtn: Button

    private val admin by lazy { ComponentName(this, AdminReceiver::class.java) }
    private val dpm by lazy { getSystemService(DevicePolicyManager::class.java) }

    private val tick = object : Runnable {
        override fun run() { refresh(); h.postDelayed(this, 1000) }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(40), dp(20), dp(40))
        }

        col.addView(text("🛡️ İffet Kalkanı", 28f, true))
        days = text("", 18f, true, 0xFF1E6B4F.toInt()); col.addView(days)
        status = text(""); col.addView(status)

        a11yBtn = button("1) Erişilebilirlik iznini aç") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }; col.addView(a11yBtn)

        adminBtn = button("2) Silinme korumasını aç") {
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Bunu açınca İffet Kalkanı dürtü anında silinemez."))
        }; col.addView(adminBtn)

        col.addView(text("\n3) Gemini API anahtarı", 18f, true))
        col.addView(text("aistudio.google.com adresinden ücretsiz alınır. Bilinmeyen siteler Gemini'ye sorulur (sadece site adı gider). Boş bırakırsan sadece hazır liste ve kelimeler çalışır.", 14f, color = 0xFF555555.toInt()))
        keyIn = EditText(this).apply {
            hint = "API anahtarı"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(Store.apiKey(this@MainActivity))
        }; col.addView(keyIn)
        modelIn = EditText(this).apply {
            hint = "Model (ör. gemini-2.5-flash)"
            setText(Store.model(this@MainActivity))
        }; col.addView(modelIn)

        col.addView(text("\nEkstra yasaklı kelimeler (virgülle ayır)", 18f, true))
        wordsIn = EditText(this).apply {
            hint = "ör: kelime1, kelime2"
            setText(Store.extraWords(this@MainActivity))
        }; col.addView(wordsIn)

        saveBtn = button("Ayarları kaydet") {
            Store.saveSettings(this, keyIn.text.toString(), modelIn.text.toString(), wordsIn.text.toString())
            Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show()
        }; col.addView(saveBtn)

        lockText = text("", 17f, true).apply { setPadding(0, dp(24), 0, 0) }
        col.addView(lockText)

        finishBtn = button("✅ Kurulumu bitir ve KİLİTLE", 0xFFB23A3A.toInt()) {
            Store.saveSettings(this, keyIn.text.toString(), modelIn.text.toString(), wordsIn.text.toString())
            Store.finishSetup(this)
            Toast.makeText(this, "Kilitlendi. Bismillah, yolun açık olsun.", Toast.LENGTH_LONG).show()
        }; col.addView(finishBtn)

        unlockBtn = button("Değişiklik yapmak istiyorum (10 dk bekle)", 0xFF6B6B6B.toInt()) {
            Store.requestUnlock(this)
        }; col.addView(unlockBtn)

        relockBtn = button("Vazgeçtim, hemen kilitle", 0xFF1E6B4F.toInt()) {
            Store.relock(this)
        }; col.addView(relockBtn)

        setContentView(ScrollView(this).apply {
            setBackgroundColor(0xFFF4F7F5.toInt())
            addView(col)
        })
    }

    override fun onResume() { super.onResume(); h.post(tick) }
    override fun onPause() { h.removeCallbacks(tick); super.onPause() }

    private fun a11yOn(): Boolean =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("$packageName/") == true

    private fun refresh() {
        val a11y = a11yOn()
        val adm = dpm.isAdminActive(admin)
        val setup = Store.setupDone(this)
        val unlocked = Store.isUnlocked(this)

        status.text = "Erişilebilirlik: ${if (a11y) "✅ açık" else "❌ kapalı"}\n" +
            "Silinme koruması: ${if (adm) "✅ açık" else "❌ kapalı"}"
        a11yBtn.visibility = if (a11y) View.GONE else View.VISIBLE
        adminBtn.visibility = if (adm) View.GONE else View.VISIBLE

        listOf(keyIn, modelIn, wordsIn, saveBtn).forEach { it.isEnabled = unlocked }
        saveBtn.alpha = if (unlocked) 1f else 0.4f

        if (!setup) {
            days.text = ""
            finishBtn.visibility = View.VISIBLE
            finishBtn.isEnabled = a11y && adm
            finishBtn.alpha = if (a11y && adm) 1f else 0.4f
            lockText.text = "Önce 1 ve 2'yi aç, ayarları yap, sonra kilitle.\nKilitledikten sonra her değişiklik için 10 dk beklemen gerekecek."
            unlockBtn.visibility = View.GONE
            relockBtn.visibility = View.GONE
            return
        }

        val d = (System.currentTimeMillis() - Store.cleanSince(this)) / 86_400_000L
        days.text = "Kalkanla geçen gün: $d"
        finishBtn.visibility = View.GONE
        val pending = Store.pendingMsLeft(this)
        val open = Store.openMsLeft(this)
        lockText.text = when {
            pending > 0 -> "⏳ Bekleme: ${fmt(pending)} kaldı.\nBitince 10 dk boyunca ayarları değiştirebilirsin. Bu sürede dürtü geçerse vazgeç."
            open > 0 -> "🔓 Kilit açık: ${fmt(open)} sonra otomatik kilitlenir."
            else -> "🔒 Kilitli. Ayarlar, erişilebilirlik ve silme ekranları kapalı."
        }
        val busy = pending > 0 || open > 0
        unlockBtn.visibility = if (busy) View.GONE else View.VISIBLE
        relockBtn.visibility = if (busy) View.VISIBLE else View.GONE
    }
}
