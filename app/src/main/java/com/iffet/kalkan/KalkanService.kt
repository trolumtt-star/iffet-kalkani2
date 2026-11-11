package com.iffet.kalkan

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
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

    /** Telegram ve Telegram'ın bilinen kopyaları. */
    private val telegramPkgs = setOf(
        "org.telegram.messenger", "org.telegram.messenger.web", "org.telegram.messenger.beta",
        "org.thunderdog.challegram", "org.telegram.plus", "tw.nekomimi.nekogram", "xyz.nextalone.nagram",
        "com.exteragram.messenger", "org.forkgram.messenger", "it.octogram.android", "uz.unnarsx.cherrygram",
        "nekox.messenger", "org.telegram.bgram", "app.nicegram", "com.iMe.android"
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
        Store.migrate(this)
        toast("İffet Kalkanı aktif. Allah yardımcın olsun 🤲")
    }

    private fun blockIncognito(pkg: String) {
        performGlobalAction(GLOBAL_ACTION_BACK)
        kick(pkg, 0L, "Gizli sekme engelli. Saklanacak bir şeyin olmasın kardeşim.")
    }

    /** Ekranda (sayfa içi dahil) gizli modda olunduğunu gösteren yazı var mı? */
    private fun inIncognito(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var seen = 0
        while (queue.isNotEmpty() && seen < 400) {
            val n = queue.removeFirst(); seen++
            for (t in listOf(n.text, n.contentDescription)) {
                if (t != null && Detector.isIncognitoScreen(Detector.normalize(t.toString()))) return true
            }
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        return false
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

        // 1b) Telegram tamamen engelli
        if (pkg in telegramPkgs) {
            if (e.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) kick(pkg, 0L, "Telegram engelli. O kapıyı kapattın, açma.")
            return
        }

        // 1c) Gizli sekme açmaya basıldı (tarayıcı menüsünde ya da uygulama simgesine uzun basınca)
        if (e.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val clicked = Detector.normalize((e.text?.joinToString(" ") ?: "") + " " + (e.contentDescription ?: ""))
            if (Detector.isIncognitoButton(clicked)) {
                blockIncognito(pkg)
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

    private var lastDomain: String? = null
    private var lastTitle = ""

    private fun checkBrowser(pkg: String) {
        val root = rootInActiveWindow ?: return
        if (inIncognito(root)) { blockIncognito(pkg); return }

        val node = browsers[pkg]!!.firstNotNullOfOrNull { id ->
            root.findAccessibilityNodeInfosByViewId(id).firstOrNull()
        }

        // --- A) Adres çubuğu ---
        if (node != null && !node.isFocused) { // odaktaysa kullanıcı yazıyor; onu klavye koruması kontrol ediyor
            val text = node.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty() && text != lastUrl) {
                lastUrl = text
                lastTitle = ""
                val domain = Detector.domainOf(text)
                lastDomain = domain
                if (domain != null && Store.isAllowed(this, domain)) return // kullanıcının izin verdiği site
                if (domain != null && Detector.isTelegramDomain(domain)) {
                    kick(pkg, 0L, "Telegram engelli. O kapıyı kapattın, açma.")
                    return
                }

                if (Detector.hasBadWord(this, text) || (domain != null && Detector.domainLooksAdult(domain))) {
                    domain?.let { Store.cacheDomain(this, it, true) }
                    punish(pkg, Store.SITE_BAN_MS, "+18 bir site açmaya çalıştın.")
                    return
                }
                if (domain != null) when {
                    Detector.isSafeDomain(domain) -> Unit // güvenli liste, eski hatalı hafızadan önce gelir
                    Detector.isBlockedDomain(domain) || Store.isCachedAdult(this, domain) -> {
                        punish(pkg, Store.SITE_BAN_MS, "+18 bir site açmaya çalıştın.")
                        return
                    }
                    Store.isCachedSafe(this, domain) -> Unit
                    else -> GeminiChecker.check(this, domain) {
                        punish(pkg, Store.SITE_BAN_MS, "Bu site (+18) olarak tespit edildi: $domain")
                    }
                }
            }
        }

        // --- B) Sayfanın adı ve üstte yazan başlıklar. Gemini çalışmasa bile bu her zaman çalışır. ---
        // Ör: doeda.com açıldı, başlıkta "TÜRKİYENİN EN BÜYÜK P*RNO SİTESİ" yazıyor -> at, 5 dk kilitle.
        lastDomain?.let {
            if (Store.isAllowed(this, it)) return
            // Güvenli sitelerde sayfa yazısı taranmaz; arama motorlarında taranır (+18 arama sonuçları için)
            if (Detector.isSafeDomain(it) && !Detector.isSearchEngine(it)) return
        }
        val texts = pageTexts(root)
        if (texts.isEmpty()) return
        val joined = texts.joinToString("\n")
        if (joined == lastTitle) return
        lastTitle = joined
        if (texts.any { Detector.hasBadWord(this, it) }) {
            lastDomain?.let { Store.cacheDomain(this, it, true) } // bir dahaki sefere adresten direkt engellensin
            punish(pkg, Store.SITE_BAN_MS, "Bu sitenin adında/başlığında +18 içerik var.")
        }
    }

    /**
     * Web sayfasının adını (<title>) ve sayfanın üst kısmındaki ilk yazıları toplar.
     * Tarayıcılar sayfa adını web içeriğinin kök düğümüne koyar.
     */
    private fun pageTexts(root: AccessibilityNodeInfo): List<String> {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var web: AccessibilityNodeInfo? = null
        var seen = 0
        while (queue.isNotEmpty() && seen < 300) {
            val n = queue.removeFirst(); seen++
            if (n.className?.toString() == "android.webkit.WebView") { web = n; break }
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        web ?: return emptyList()

        val out = ArrayList<String>()
        (web.text ?: web.contentDescription)?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { out.add(it) }

        // Sayfanın üstündeki ilk 15 yazı (logo yazısı, büyük başlık, menü...)
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(web)
        seen = 0
        while (stack.isNotEmpty() && seen < 200 && out.size < 15) {
            val n = stack.removeFirst(); seen++
            if (n !== web) {
                (n.text ?: n.contentDescription)?.toString()?.trim()
                    ?.takeIf { it.length in 2..300 }?.let { out.add(it) }
            }
            for (i in 0 until n.childCount) n.getChild(i)?.let { stack.add(it) }
        }
        return out
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
        lastTitle = ""
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
