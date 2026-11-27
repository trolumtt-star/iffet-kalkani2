package com.iffet.kalkan

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Telefonun saatinden BAĞIMSIZ saat. Saati/tarihi elle değiştirmek bekleme sürelerini ve kilitleri bitirmez.
 *
 * Nasıl çalışır:
 *  - Telefon açıldığından beri geçen süre (elapsedRealtime) kullanıcı tarafından değiştirilemez.
 *    Saat = son güvenilir zaman + o andan beri geçen gerçek süre.
 *  - Telefon yeniden başlatılınca bu sayaç sıfırlanır. O zaman internetten (Google sunucusu, Android ağ saati)
 *    gerçek saat alınır. İnternet yoksa en son bilinen zamandan devam edilir; yani kapalıyken geçen süre
 *    sayılmaz, ama saati ileri almak asla işe yaramaz.
 *  - İnternet geldiğinde saat gerçek zamana göre düzeltilir.
 */
object TrustedClock {
    private const val PREF = "kalkan_clock"
    private const val SAVE_EVERY_MS = 30_000L

    @Volatile private var anchorTrusted = 0L
    @Volatile private var anchorElapsed = 0L
    @Volatile private var anchorBoot = -1
    @Volatile private var loaded = false
    @Volatile private var lastSaved = 0L
    @Volatile private var lastNetSync = 0L

    private fun p(c: Context) = c.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    /** Kaçıncı açılış. Uygulama süreci yeniden başlatmada zaten öldüğü için bir kere okunması yeter. */
    @Volatile private var cachedBoot: Int? = null
    private fun bootCount(c: Context): Int = cachedBoot ?: (
        try { Settings.Global.getInt(c.contentResolver, Settings.Global.BOOT_COUNT) } catch (_: Exception) { -1 }
    ).also { cachedBoot = it }

    @Synchronized
    fun now(c: Context): Long {
        if (!loaded) load(c)
        val elapsed = SystemClock.elapsedRealtime()
        val boot = bootCount(c)

        if (anchorTrusted == 0L || boot != anchorBoot || elapsed < anchorElapsed) {
            // İlk çalışma ya da telefon yeniden başlatıldı: önce Android'in ağ saatine bak
            val base = systemNetworkTime()
                ?: p(c).getLong("last_trusted", 0L).takeIf { it > 0 }  // en son bilinen zaman
                ?: System.currentTimeMillis()                          // sadece ilk kurulumda
            setAnchor(c, base, elapsed, boot)
        }

        val t = anchorTrusted + (elapsed - anchorElapsed)
        if (elapsed - lastSaved > SAVE_EVERY_MS) {
            lastSaved = elapsed
            p(c).edit().putLong("last_trusted", t).apply()
        }
        return t
    }

    /** İnternetten gerçek saati alıp düzeltir. Servis açılınca ve 15 dakikada bir çağrılır. */
    fun syncFromNetwork(c: Context) {
        val app = c.applicationContext
        val elapsedNow = SystemClock.elapsedRealtime()
        if (lastNetSync != 0L && elapsedNow - lastNetSync < 15 * 60_000L) return
        lastNetSync = elapsedNow
        thread {
            val net = systemNetworkTime() ?: httpTime() ?: return@thread
            synchronized(this) {
                setAnchor(app, net, SystemClock.elapsedRealtime(), bootCount(app))
            }
        }
    }

    private fun setAnchor(c: Context, trusted: Long, elapsed: Long, boot: Int) {
        anchorTrusted = trusted; anchorElapsed = elapsed; anchorBoot = boot
        p(c).edit()
            .putLong("anchor_trusted", trusted).putLong("anchor_elapsed", elapsed)
            .putInt("anchor_boot", boot).putLong("last_trusted", trusted)
            .apply()
    }

    private fun load(c: Context) {
        val pr = p(c)
        anchorTrusted = pr.getLong("anchor_trusted", 0L)
        anchorElapsed = pr.getLong("anchor_elapsed", 0L)
        anchorBoot = pr.getInt("anchor_boot", -1)
        loaded = true
    }

    /** Android 13+: telefonun internetten aldığı saat (kullanıcının elle ayarladığı saat değil). */
    private fun systemNetworkTime(): Long? = try {
        if (android.os.Build.VERSION.SDK_INT >= 33) SystemClock.currentNetworkTimeClock().millis() else null
    } catch (_: Exception) { null }

    /** Google sunucusunun cevabındaki saat bilgisi. */
    private fun httpTime(): Long? = try {
        val conn = (URL("https://www.google.com/generate_204").openConnection() as HttpURLConnection).apply {
            requestMethod = "HEAD"; connectTimeout = 8000; readTimeout = 8000; useCaches = false
        }
        conn.connect()
        val d = conn.date
        conn.disconnect()
        d.takeIf { it > 0 }
    } catch (_: Exception) { null }
}
