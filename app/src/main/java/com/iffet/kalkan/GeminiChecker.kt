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
 * Bilinmeyen bir siteyi Gemini'ye sorar. Gizlilik için SADECE alan adı gönderilir (ör. "ornek.com"),
 * sayfanın tam adresi veya içeriği gönderilmez.
 */
object GeminiChecker {
    private val inFlight = Collections.synchronizedSet(HashSet<String>())
    private val main = Handler(Looper.getMainLooper())

    fun check(c: Context, domain: String, onAdult: () -> Unit) {
        val key = Store.apiKey(c)
        if (key.isBlank()) return
        if (!inFlight.add(domain)) return
        val app = c.applicationContext

        thread {
            try {
                val prompt = "Sadece tek kelimeyle EVET ya da HAYIR yaz. " +
                    "Şu alan adı pornografik, çıplaklık veya +18 cinsel içerik sunan bir site mi? " +
                    "Anime, oyun, sosyal medya, haber veya yapay zeka siteleri HAYIR'dır. Alan adı: $domain"

                val body = JSONObject()
                    .put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    ))
                    .put("generationConfig", JSONObject().put("temperature", 0))

                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${Store.model(app)}:generateContent")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", key)
                }
                conn.outputStream.use { it.write(body.toString().toByteArray()) }

                if (conn.responseCode == 200) {
                    val res = JSONObject(conn.inputStream.bufferedReader().readText())
                    val text = res.getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).optString("text", "")
                    val adult = text.uppercase().contains("EVET")
                    Store.cacheDomain(app, domain, adult)
                    if (adult) main.post(onAdult)
                }
                conn.disconnect()
            } catch (_: Exception) {
                // İnternet yoksa veya anahtar hatalıysa siteyi engellemeyiz; bir sonraki açılışta tekrar sorulur.
            } finally {
                inFlight.remove(domain)
            }
        }
    }
}
