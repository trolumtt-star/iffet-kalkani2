package com.iffet.kalkan

import android.content.Context
import java.util.Locale

object Detector {
    private val tr = Locale("tr", "TR")

    /** Metnin içinde geçmesi yeterli olanlar. */
    private val partWords = listOf(
        "porn", "xvideos", "xnxx", "xhamster", "redtube", "youporn", "spankbang",
        "onlyfans", "brazzers", "chaturbate", "stripchat", "bongacams", "livejasmin",
        "hentai", "rule34", "nhentai", "sikis", "ifsa", "+18", "18+", "nsfw",
        "erotik", "erotic", "milf", "bdsm", "camgirl", "xxx", "turbanli ifsa"
    )

    /** Sadece ayrı kelime olarak yakalananlar ("seksen", "essex" gibi kelimeler yanlışlıkla engellenmesin diye). */
    private val wholeWords = Regex(
        "(^|[^a-z0-9])(sex|seks|sexy|seksi|nude|nudes|naked|ciplak|escort|amcik)([^a-z0-9]|$)"
    )

    fun normalize(s: String): String = s.lowercase(tr)
        .replace('ı', 'i').replace('ş', 's').replace('ç', 'c')
        .replace('ğ', 'g').replace('ü', 'u').replace('ö', 'o')
        .replace("​", "").replace("‌", "").replace("‍", "")

    fun hasBadWord(c: Context, raw: String): Boolean {
        val n = normalize(raw)
        if (partWords.any { n.contains(it) }) return true
        if (wholeWords.containsMatchIn(n)) return true
        val extra = Store.extraWords(c).split(',').map { normalize(it.trim()) }.filter { it.length >= 2 }
        return extra.any { n.contains(it) }
    }

    // --- Siteler ---
    private val blockedDomains = setOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com", "youporn.com",
        "spankbang.com", "onlyfans.com", "chaturbate.com", "stripchat.com", "eporner.com",
        "tube8.com", "brazzers.com", "txxx.com", "hqporner.com", "motherless.com", "rule34.xxx",
        "nhentai.net", "beeg.com", "youjizz.com", "tnaflix.com", "cam4.com", "bongacams.com",
        "livejasmin.com", "erome.com", "fapello.com", "porntrex.com", "hanime.tv", "e-hentai.org",
        "fansly.com", "manyvids.com", "sxyprn.com", "thothub.to", "hentaihaven.xxx"
    )

    /** Bunlar için Gemini'ye sorulmaz (hız ve kota için). Anime, yapay zeka vs. çalışmaya devam eder. */
    private val safeDomains = setOf(
        "google.com", "google.com.tr", "youtube.com", "gemini.google.com", "chatgpt.com",
        "openai.com", "claude.ai", "anthropic.com", "wikipedia.org", "instagram.com",
        "whatsapp.com", "web.whatsapp.com", "twitter.com", "x.com", "facebook.com", "tiktok.com",
        "github.com", "stackoverflow.com", "trendyol.com", "hepsiburada.com", "sahibinden.com",
        "eksisozluk.com", "diyanet.gov.tr", "turkiye.gov.tr", "kuran.diyanet.gov.tr",
        "netflix.com", "crunchyroll.com", "myanimelist.net", "spotify.com", "bing.com",
        "duckduckgo.com", "yandex.com", "yandex.com.tr", "amazon.com.tr", "linkedin.com"
    )

    /** Adres çubuğundaki yazıdan alan adını çıkarır. Arama sorgusuysa null döner. */
    fun domainOf(text: String): String? {
        var t = text.trim().lowercase(Locale.ROOT)
        if (t.isEmpty() || t.contains(' ')) return null
        t = t.substringAfter("://")
        t = t.substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':')
        t = t.removePrefix("www.").removePrefix("m.")
        if (!t.contains('.')) return null
        if (!Regex("^[a-z0-9.-]+$").matches(t)) return null
        return t
    }

    private fun inSet(d: String, set: Set<String>) = set.any { d == it || d.endsWith(".$it") }
    fun isBlockedDomain(d: String) = inSet(d, blockedDomains) || d.endsWith(".xxx") || d.endsWith(".porn") || d.endsWith(".sex") || d.endsWith(".adult")
    fun isSafeDomain(d: String) = inSet(d, safeDomains)
}
