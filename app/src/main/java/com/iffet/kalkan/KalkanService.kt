package com.iffet.kalkan

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class KalkanService : AccessibilityService() {

    /** Uygulamayı kapatmak/silmek için girilebilecek sistem ekranları. */
    private val settingsPkgs = setOf(
        "com.android.settings", "com.android.packageinstaller", "com.google.android.packageinstaller",
        "com.android.permissioncontroller", "com.google.android.permissioncontroller",
        "com.miui.securitycenter", "com.miui.packageinstaller", "com.coloros.safecenter",
        "com.oplus.safecenter", "com.huawei.systemmanager", "com.samsung.android.lool",
        "com.samsung.accessibility", "com.google.android.accessibility.suite"
    )

    /** Tarayıcıların adres çubuğu kimlikleri. */
    private val browsers = mapOf(
        "com.android.chrome" to listOf("com.android.chrome:id/url_bar"),
        "com.chrome.beta" to listOf("com.chrome.beta:id/url_bar"),
        "com.brave.browser" to listOf("com.brave.browser:id/url_bar"),
        "com.microsoft.emmx" to listOf("com.microsoft.emmx:id/url_bar"),
        "com.kiwibrowser.browser" to listOf("com.kiwibrowser.browser:id/url_bar"),
        "com.vivaldi.browser" to listOf("com.vivaldi.browser:id/url_bar"),
        "com.opera.browser" to listOf("com.opera.browser:id/url_field"),
        "com.opera.mini.native" to listOf("com.opera.mini.native:id/url_field"),
        "org.mozilla.firefox" to listOf("org.mozilla.firefox:id/mozac_browser_toolbar_url_view"),
        "com.sec.android.app.sbrowser" to listOf("com.sec.android.app.sbrowser:id/location_bar_edit_text"),
        "com.yandex.browser" to listOf("com.yandex.browser:id/bro_omnibar_address_title_text"),
        "com.duckduckgo.mobile.android" to listOf("com.duckduckgo.mobile.android:id/omnibarTextInput"),
        "com.mi.globalbrowser" to listOf("com.mi.globalbrowser:id/url")
    )

    private val main = Handler(Looper.getMainLooper())
    private var lastSettingsScan = 0L
    private var lastBrowserScan = 0L
    private var lastUrl = ""
    private var lastKick = 0L

    private val homePkgs: Set<String> by lazy {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.queryIntentActivities(i, 0).map { it.activityInfo.packageName }.toSet()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        toast("İffet Kalkanı aktif. Allah yardımcın olsun 🤲")
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        e ?: return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return
        val now = System.currentTimeMillis()

        // 1) Yasaklı uygulamaya girmeye çalışıyor
        if (e.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val until = Store.bannedUntil(this, pkg)
            if (until > now) {
                kick(pkg, until, "Bu uygulama şu an kilitli. Biraz sabret.")
                return
            }
        }

        // 2) Kilitliyken bu uygulamanın ayarlarına / silme ekranına girmeye çalışıyor
        if (pkg in settingsPkgs && !Store.isUnlocked(this) && now - lastSettingsScan > 300) {
            lastSettingsScan = now
            if (screenMentionsUs()) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)
                toast("Kalkan kilitli. Değişiklik için uygulamadan 10 dk bekleme başlat.")
                return
            }
        }

        // 3) Klavyeyle +18 bir şey yazdı (her uygulamada)
        if (e.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED && !e.isPassword) {
            val txt = e.text?.joinToString(" ") ?: ""
            if (txt.isNotBlank() && Detector.hasBadWord(this, txt)) {
                punish(pkg, Store.TYPE_BAN_MS, "+18 bir şey yazdın.")
                return
            }
        }

        // 4) Tarayıcıda açılan site
        if (pkg in browsers && now - lastBrowserScan > 700) {
            lastBrowserScan = now
            checkBrowser(pkg)
        }
    }

    private fun checkBrowser(pkg: String) {
        val root = rootInActiveWindow ?: return
        val node = browsers[pkg]!!.firstNotNullOfOrNull { id ->
            root.findAccessibilityNodeInfosByViewId(id).firstOrNull()
        } ?: return
        if (node.isFocused) return // kullanıcı yazıyor; yazılanı 3. adım zaten kontrol ediyor
        val text = node.text?.toString()?.trim() ?: return
        if (text.isEmpty() || text == lastUrl) return
        lastUrl = text

        if (Detector.hasBadWord(this, text)) {
            punish(pkg, Store.SITE_BAN_MS, "+18 bir site açmaya çalıştın.")
            return
        }
        val domain = Detector.domainOf(text) ?: return
        when {
            Detector.isBlockedDomain(domain) || Store.isCachedAdult(this, domain) ->
                punish(pkg, Store.SITE_BAN_MS, "+18 bir site açmaya çalıştın.")
            Detector.isSafeDomain(domain) || Store.isCachedSafe(this, domain) -> Unit
            else -> GeminiChecker.check(this, domain) {
                punish(pkg, Store.SITE_BAN_MS, "Bu site (+18) olarak tespit edildi: $domain")
            }
        }
    }

    private fun screenMentionsUs(): Boolean {
        val root = rootInActiveWindow ?: return false
        return root.findAccessibilityNodeInfosByText("ffet Kalkan").isNotEmpty()
    }

    /** Ana ekranı ve sistem ekranlarını asla yasaklamayız, yoksa telefon kilitlenir. */
    private fun canBan(pkg: String) = pkg !in settingsPkgs && pkg !in homePkgs

    private fun punish(pkg: String, ms: Long, reason: String) {
        val until = System.currentTimeMillis() + ms
        if (canBan(pkg)) Store.ban(this, pkg, until)
        lastUrl = "" // yasak bitip geri dönünce aynı sekme tekrar kontrol edilsin
        kick(pkg, until, reason)
    }

    private fun kick(pkg: String, until: Long, reason: String) {
        if (pkg in browsers) performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_HOME)

        val now = System.currentTimeMillis()
        if (now - lastKick < 1500) return // uyarı ekranı zaten açılıyor
        lastKick = now

        val i = Intent(this, WarningActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("app", appLabel(pkg))
            .putExtra("until", if (canBan(pkg)) until else 0L)
            .putExtra("reason", reason)
        main.postDelayed({ startActivity(i) }, 300)
    }

    private fun appLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) { pkg }

    private fun toast(msg: String) = main.post { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }

    override fun onInterrupt() {}
}
