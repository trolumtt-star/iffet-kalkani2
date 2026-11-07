# İffet Kalkanı (Android)

Kişisel kullanım için porno engelleyici. İnternete / DNS'e dokunmaz, yani anime siteleri ve yapay zeka uygulamaları normal çalışır.

## Ne yapar
- **Klavye koruması:** Herhangi bir uygulamada +18 kelime yazarsan ana ekrana atar, uyarı ekranı açar, o uygulama **2 dk** açılmaz.
- **Site koruması:** Tarayıcıda açılan siteyi kontrol eder (hazır liste → önbellek → Gemini). +18 ise atar, tarayıcı **5 dk** açılmaz.
- **Uyarı ekranı:** Her seferinde farklı bir ayet/hadis ve dürtüyü geçirecek bir öneri gösterir.
- **10 dk kilit:** Kurulumu bitirince ayar değiştirmek için 10 dk beklersin, sonra 10 dk açık kalır ve kendiliğinden kilitlenir. Uygulama kapansa da süre işler.
- **Silinme/kapatma koruması:** Kilitliyken bu uygulamanın Ayarlar sayfası, erişilebilirlik sayfası, cihaz yöneticisi ve silme ekranı açılamaz.

## Kurulum
1. Android Studio'yu kur, bu klasörü **Open** ile aç (Gradle kendisi indirir).
2. Telefonda Geliştirici Seçenekleri → USB hata ayıklama'yı aç, telefonu bağla, ▶ Run.
3. Uygulamayı aç:
   - **1) Erişilebilirlik iznini aç** → İffet Kalkanı'nı aç.
     Android 13+ "Kısıtlı ayar" derse: Ayarlar → Uygulamalar → İffet Kalkanı → sağ üst ⋮ → **Kısıtlı ayarlara izin ver**, sonra tekrar dene.
   - **2) Silinme korumasını aç** → Etkinleştir.
   - **3)** aistudio.google.com → "Get API key" ile ücretsiz anahtarı al, yapıştır.
   - **Kurulumu bitir ve KİLİTLE.**
4. Xiaomi/Huawei/Oppo'daysan: pil ayarlarında uygulamaya "kısıtlama yok" ver ki sistem servisi kapatmasın.

## Dürüst not
Hiçbir engelleyici %100 değildir: güvenli mod, sıfırlama veya bilgisayardan ADB ile aşılabilir. Bu uygulamanın amacı dürtü anında araya 10 dakika koymak. O 10 dakika genelde yetiyor.
Kilitliyken Ayarlar'da uygulama adının geçtiği her sayfa (ör. tüm uygulamalar listesi) kapanır, bu normal.

Ayarları değiştirmek istersen: `Store.kt` (süreler), `Detector.kt` (kelimeler, siteler), `Content.kt` (ayet/hadis ve öneriler).
