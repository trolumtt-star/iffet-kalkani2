package com.iffet.kalkan

import android.content.Context
import java.util.Locale

object Detector {
    private val tr = Locale("tr", "TR")

    /**
     * Metnin içinde geçmesi yeterli olanlar. SADECE açıkça cinsel olan kelimeler.
     * (Eskiden "sıkış", "yetişkin", "ifşa", "xxx" de vardı; "sıkışma", "yetişkin bileti",
     * haberlerdeki "ifşa" ve "05xx xxx xx xx" numara kalıbı yüzünden masum şeyler engelleniyordu.)
     */
    private val partWords = listOf(
        "porn", "xvideos", "xnxx", "xhamster", "redtube", "youporn", "spankbang",
        "onlyfans", "brazzers", "chaturbate", "stripchat", "bongacams", "livejasmin",
        "hentai", "rule34", "nhentai", "nsfw", "erotik", "erotic", "milf", "bdsm", "camgirl",
        "blowjob", "xxx video", "xxx izle",
        // "ifşa" tek başına haberlerde çok geçiyor, sadece cinsel kalıpları
        "ifsa video", "ifsa izle", "ifsa grubu", "ifsa kanali", "turbanli ifsa", "liseli ifsa",
        "unlu ifsa", "onlyfans ifsa", "ifsa telegram"
    )

    /** Sadece ayrı kelime olarak yakalananlar ("seksen", "essex" gibi kelimeler yanlışlıkla engellenmesin diye). */
    private val wholeWords = Regex(
        "(^|[^a-z0-9])(sex|seks|sexy|seksi|nude|nudes|escort|amcik)([^a-z0-9]|$)"
    )

    /**
     * "sikiş" kontrolü noktalı/noktasız i ayrımıyla yapılır: "sikiş" engellenir, "sıkış/sıkışma/sıkıştı" engellenmez.
     * Bu yüzden ı -> i çevrilmeden bakılır.
     */
    private val trSexWords = Regex("(^|[^a-zıçğöşü])(sikis|sikiş|sikişme|sikisme|sikiş izle)")

    /**
     * Sansürlü/gizlenmiş yazılsa da yakalananlar: "P*RNO", "p0rn0", "p.o.r.n.o", "s1k1ş", "pоrno" (Kiril o).
     * Yazının boşluk ve noktaları silinip kontrol edilir, '*' gibi işaretler herhangi bir harf yerine sayılır.
     */
    private val fuzzyWords = listOf(
        "porno", "porn", "hentai", "xvideos", "xnxx", "xhamster", "onlyfans", "erotik", "blowjob", "nsfw"
    )

    private val lookalikes = mapOf(
        '0' to 'o', '1' to 'i', '!' to 'i', '|' to 'i', '3' to 'e', '€' to 'e', '4' to 'a', '@' to 'a',
        '5' to 's', '$' to 's', '7' to 't', '8' to 'b',
        // Kiril harfleri (Latin gibi görünen)
        'о' to 'o', 'р' to 'p', 'а' to 'a', 'е' to 'e', 'с' to 'c', 'х' to 'x', 'і' to 'i', 'к' to 'k',
        // Diğer aksanlı harfler
        'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'à' to 'a', 'á' to 'a', 'â' to 'a', 'ä' to 'a',
        'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ù' to 'u', 'ú' to 'u', 'û' to 'u'
    )

    /** Harf yerine kullanılan sansür işaretleri. */
    private val maskChars = setOf('*', '#', '?', '%', '•', '●', '★')

    /**
     * "+18" / "18+" sadece tek başınayken sayılır. Telefon numaraları (+18005551234, ABD numaraları)
     * ve "18+5" gibi hesaplar engellenmez.
     */
    private val plus18 = Regex("(^|[^0-9+])(\\+18|18\\+)([^0-9]|$)")

    fun normalize(s: String): String = s.lowercase(tr)
        .replace('ı', 'i').replace('ş', 's').replace('ç', 'c')
        .replace('ğ', 'g').replace('ü', 'u').replace('ö', 'o')
        .replace("​", "").replace("‌", "").replace("‍", "")

    /** Tek bir kelimeyi temizler: "p.o.r.n.o" -> "porno", "P*RN0" -> "p*rno". */
    private fun cleanToken(t: String): String {
        val sb = StringBuilder()
        for (ch in t) {
            val c = lookalikes[ch] ?: ch
            when {
                c in 'a'..'z' -> sb.append(c)
                c in maskChars -> sb.append('*')
                // nokta, tire, alt çizgi vb. atlanır
            }
        }
        return sb.toString()
    }

    /**
     * Metni kelimelere ayırır ve temizler. Harf harf boşlukla yazılanları birleştirir ("p o r n o" -> "porno"),
     * ama normal kelimeleri birleştirmez ("kitap örneği" yanlışlıkla "kitapornegi" olmasın diye).
     */
    private fun squeeze(n: String): List<String> {
        val out = ArrayList<String>()
        val single = StringBuilder()
        for (raw in n.split(Regex("\\s+"))) {
            val t = cleanToken(raw)
            if (t.isEmpty()) continue
            if (t.length == 1) { single.append(t); continue }
            if (single.isNotEmpty()) { out.add(single.toString()); single.clear() }
            out.add(t)
        }
        if (single.isNotEmpty()) out.add(single.toString())
        return out
    }

    /** "s1k1ş", "s*kiş" gibi sansürlü yazılar için; ı harfi korunur ki "sıkış" yakalanmasın. */
    private fun squeezeLight(light: String): List<String> = light.split(Regex("\\s+")).map { raw ->
        val sb = StringBuilder()
        for (ch in raw) {
            val c = lookalikes[ch] ?: ch
            when {
                c in 'a'..'z' || c == 'ı' -> sb.append(c)
                c in maskChars -> sb.append('*')
            }
        }
        sb.toString()
    }.filter { it.length >= 4 }

    /** Kelimenin her harfi ya aynı ya da '*' olabilir. En az yarısı gerçek harf olmalı. */
    private fun fuzzyContains(s: String, word: String): Boolean {
        val maxMask = word.length / 2
        outer@ for (i in 0..s.length - word.length) {
            var masks = 0
            for (j in word.indices) {
                val c = s[i + j]
                if (c == word[j]) continue
                if (c == '*' && ++masks <= maxMask) continue
                continue@outer
            }
            return true
        }
        return false
    }

    fun hasBadWord(c: Context, raw: String): Boolean {
        val n = normalize(raw)
        if (partWords.any { n.contains(it) }) return true
        if (wholeWords.containsMatchIn(n)) return true
        if (plus18.containsMatchIn(n)) return true
        val light = raw.lowercase(tr).replace('ş', 's').replace('İ', 'i')
        if (trSexWords.containsMatchIn(light)) return true
        if (squeezeLight(light).any { fuzzyContains(it, "sikis") }) return true
        val tokens = squeeze(n)
        if (tokens.any { tok -> fuzzyWords.any { fuzzyContains(tok, it) } }) return true
        val extra = Store.extraWords(c).split(',').map { normalize(it.trim()) }.filter { it.length >= 2 }
        return extra.any { w ->
            n.contains(w) || cleanToken(w).let { cw -> cw.length >= 3 && tokens.any { fuzzyContains(it, cw) } }
        }
    }

    /** Alan adının içine gizlenmiş kelimeler: hotsexvideos.net, freeporn4u.com, sikisizle.org ... */
    private val domainParts = listOf(
        "porn", "sex", "xxx", "nude", "nsfw", "pussy", "milf",
        "erotic", "erotik", "sikis", "onlyfan", "xvideo", "xnxx", "xhamster", "fapello", "camgirl",
        "18plus", "hentai"
    )
    /** "sex" ve "jav" geçen ama masum olan alan adları. */
    private val domainExceptions = listOf(
        "essex", "sussex", "middlesex", "wessex", "sexton", "unisex", "sextant"
    )

    fun domainLooksAdult(domain: String): Boolean {
        if (isSafeDomain(domain)) return false
        var d = domain
        domainExceptions.forEach { d = d.replace(it, "") }
        return domainParts.any { d.contains(it) }
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
        "duckduckgo.com", "yandex.com", "yandex.com.tr", "amazon.com.tr", "linkedin.com",
        "vapi.ai", "supabase.com", "vercel.com", "aistudio.google.com"
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

    fun isTelegramDomain(d: String) =
        inSet(d, setOf("telegram.org", "t.me", "telegram.me", "telegram.dog", "telesco.pe", "tgstat.com", "tgstat.ru"))

    // --- Gizli sekme ---
    /** Gizli sekme açan butonlar/menü öğeleri (Chrome, Brave, Edge, Firefox, Samsung, Opera, Yandex). */
    private val incognitoButtons = listOf(
        "gizli sekme", "incognito", "inprivate", "private tab", "ozel sekme", "gizli mod",
        "secret mode", "private window", "gizli pencere", "ozel pencere", "gizli gezinti", "private browsing"
    )

    /** Gizli moddayken ekranda çıkan yazılar. (Menüdeki "yeni gizli sekme" yazısını görmek için değil.) */
    private val incognitoScreens = listOf(
        "gizli moda gectiniz", "gizli moddasiniz", "you've gone incognito", "you’ve gone incognito",
        "gizli moddasin", "inprivate gezinti", "inprivate browsing", "you're in inprivate",
        "private browsing is on", "ozel gezinti", "gizli gezinti", "you're in a private",
        "secret mode is on", "gizli mod acik", "gizli moda gecildi"
    )

    fun isIncognitoButton(n: String) = incognitoButtons.any { n.contains(it) }
    fun isIncognitoScreen(n: String) = incognitoScreens.any { n.contains(it) }
    fun isSearchEngine(d: String) =
        inSet(d, setOf("google.com", "google.com.tr", "bing.com", "yandex.com", "yandex.com.tr", "duckduckgo.com"))
}
