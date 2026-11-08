package com.iffet.kalkan

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import kotlin.concurrent.thread

/**
 * Bilinmeyen bir siteyi Gemini'ye sorar. Gizlilik için SADECE alan adı gönderilir (ör. "ornek.com").
 *
 * Birden fazla anahtar ve model desteklenir: biri kotası dolunca (429) veya model yoksa (404)
 * otomatik olarak sıradaki anahtar/model denenir. Dolan kombinasyon bir süre atlanır.
 */
object GeminiChecker {
    private const val SKIP_QUOTA_MS = 30 * 60_000L      // kotası dolan anahtar+model 30 dk atlanır
    private const val SKIP_MISSING_MS = 24 * 3_600_000L // olmayan model 1 gün atlanır

    private val inFlight = Collections.synchronizedSet(HashSet<String>())
    private val skipUntil = Collections.synchronizedMap(HashMap<String, Long>())
    private val main = Handler(Looper.getMainLooper())

    /** Ayarlar ekranında gösterilen son durum. */
    @Volatile var lastStatus: String = "Henüz soru sorulmadı."
        private set

    fun check(c: Context, domain: String, onAdult: () -> Unit) {
        val keys = Store.apiKeys(c)
        if (keys.isEmpty()) return
        if (!inFlight.add(domain)) return
        val app = c.applicationContext
        val models = Store.models(app)

        thread {
            try {
                for (key in keys) for (model in models) {
                    val combo = "${key.takeLast(6)}|$model"
                    if ((skipUntil[combo] ?: 0L) > System.currentTimeMillis()) continue

                    when (val r = ask(key, model, domain)) {
                        is Result.Answer -> {
                            Store.cacheDomain(app, domain, r.adult)
                            lastStatus = "✅ Çalışıyor: $model (anahtar …${key.takeLast(4)})"
                            if (r.adult) main.post(onAdult)
                            return@thread
                        }
                        Result.Quota -> skipUntil[combo] = System.currentTimeMillis() + SKIP_QUOTA_MS
                        Result.Missing -> skipUntil[combo] = System.currentTimeMillis() + SKIP_MISSING_MS
                        Result.BadKey -> {
                            // Bu anahtar hiçbir modelde çalışmaz, hepsini atla
                            models.forEach { skipUntil["${key.takeLast(6)}|$it"] = System.currentTimeMillis() + SKIP_MISSING_MS }
                            break
                        }
                        Result.NoNet -> { lastStatus = "📵 İnternet yok, sonra tekrar sorulacak."; return@thread }
                    }
                }
                lastStatus = "⚠️ Bütün anahtarların/modellerin kotası dolu. Hazır liste ve kelime koruması çalışmaya devam ediyor."
            } finally {
                inFlight.remove(domain)
            }
        }
    }

    private sealed class Result {
        data class Answer(val adult: Boolean) : Result()
        object Quota : Result()
        object Missing : Result()
        object BadKey : Result()
        object NoNet : Result()
    }

    private fun ask(key: String, model: String, domain: String): Result {
        return try {
            val prompt = "Sadece tek kelimeyle EVET ya da HAYIR yaz. " +
                "Şu alan adı pornografik, çıplaklık veya +18 cinsel içerik sunan bir site mi? " +
                "Anime, oyun, sosyal medya, haber veya yapay zeka siteleri HAYIR'dır. Alan adı: $domain"

            val body = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                ))
                .put("generationConfig", JSONObject().put("temperature", 0))

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", key)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val result = when (code) {
                200 -> {
                    val res = JSONObject(conn.inputStream.bufferedReader().readText())
                    val text = res.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text", "") ?: ""
                    Result.Answer(text.uppercase().contains("EVET"))
                }
                429, 500, 503 -> Result.Quota          // kota doldu veya model yoğun
                404 -> Result.Missing                  // model adı yanlış / kaldırılmış
                400, 401, 403 -> Result.BadKey         // anahtar geçersiz
                else -> Result.Quota
            }
            conn.disconnect()
            result
        } catch (_: java.io.IOException) {
            Result.NoNet
        } catch (_: Exception) {
            Result.Quota
        }
    }
}
