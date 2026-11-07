package com.iffet.kalkan

import android.content.Context

/** Tüm ayarlar ve zamanlayıcılar. Zamanlar "başlangıç anı" olarak saklanır, yani uygulama kapansa da süre işler. */
object Store {
    const val WAIT_MS = 10 * 60_000L        // değişiklik için bekleme
    const val OPEN_MS = 10 * 60_000L        // bekleme bitince ayarların açık kaldığı süre
    const val TYPE_BAN_MS = 2 * 60_000L     // +18 yazınca uygulama yasağı
    const val SITE_BAN_MS = 5 * 60_000L     // +18 site açınca tarayıcı yasağı

    private fun p(c: Context) = c.getSharedPreferences("kalkan", Context.MODE_PRIVATE)
    private fun now() = System.currentTimeMillis()

    // --- Kurulum ---
    fun setupDone(c: Context) = p(c).getBoolean("setup_done", false)
    fun finishSetup(c: Context) =
        p(c).edit().putBoolean("setup_done", true).putLong("clean_since", now()).apply()
    fun cleanSince(c: Context) = p(c).getLong("clean_since", now())

    // --- 10 dakikalık kilit ---
    private fun req(c: Context) = p(c).getLong("unlock_req", 0L)

    fun requestUnlock(c: Context) {
        if (pendingMsLeft(c) > 0 || openMsLeft(c) > 0) return
        p(c).edit().putLong("unlock_req", now()).apply()
    }

    fun relock(c: Context) = p(c).edit().remove("unlock_req").apply()

    fun pendingMsLeft(c: Context): Long {
        val r = req(c); if (r == 0L) return 0
        val t = now() - r
        return if (t in 0 until WAIT_MS) WAIT_MS - t else 0
    }

    fun openMsLeft(c: Context): Long {
        val r = req(c); if (r == 0L) return 0
        val t = now() - r
        return if (t in WAIT_MS until WAIT_MS + OPEN_MS) WAIT_MS + OPEN_MS - t else 0
    }

    /** Kurulum bitmediyse her şey açık; bittiyse sadece 10 dk beklemeden sonraki pencerede açık. */
    fun isUnlocked(c: Context) = !setupDone(c) || openMsLeft(c) > 0

    // --- Uygulama yasakları ---
    fun ban(c: Context, pkg: String, until: Long) {
        if (until > bannedUntil(c, pkg)) p(c).edit().putLong("ban_$pkg", until).apply()
    }
    fun bannedUntil(c: Context, pkg: String) = p(c).getLong("ban_$pkg", 0L)

    // --- Ayarlar ---
    fun apiKey(c: Context) = p(c).getString("api_key", "") ?: ""
    fun model(c: Context) = p(c).getString("model", "gemini-2.5-flash") ?: "gemini-2.5-flash"
    fun extraWords(c: Context) = p(c).getString("extra_words", "") ?: ""

    fun saveSettings(c: Context, key: String, model: String, words: String) {
        p(c).edit()
            .putString("api_key", key.trim())
            .putString("model", model.trim().ifEmpty { "gemini-2.5-flash" })
            .putString("extra_words", words)
            .apply()
    }

    // --- Gemini sonuç önbelleği (aynı siteyi tekrar tekrar sormamak için) ---
    fun isCachedAdult(c: Context, d: String) = p(c).getStringSet("adult", emptySet())!!.contains(d)
    fun isCachedSafe(c: Context, d: String) = p(c).getStringSet("safe", emptySet())!!.contains(d)

    fun cacheDomain(c: Context, d: String, adult: Boolean) {
        val k = if (adult) "adult" else "safe"
        val set = HashSet(p(c).getStringSet(k, emptySet())!!)
        set.add(d)
        p(c).edit().putStringSet(k, set).apply()
    }
}
