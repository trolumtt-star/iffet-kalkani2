package com.iffet.kalkan

/** Ayet mealleri ve hadisler (anlam olarak aktarılmıştır, kaynakları yanında). */
object Content {
    val reminders = listOf(
        "Mü'min erkeklere söyle: Gözlerini haramdan sakınsınlar ve iffetlerini korusunlar. Bu, onlar için daha temizdir. Allah yaptıklarından tamamen haberdardır." to "Nur Suresi, 30. ayet",
        "Zinaya yaklaşmayın bile. Çünkü o bir hayasızlıktır ve çok kötü bir yoldur." to "İsra Suresi, 32. ayet",
        "Kulak, göz ve kalp; bunların her biri yaptıklarından sorumludur." to "İsra Suresi, 36. ayet",
        "De ki: Ey nefislerine karşı aşırı giden kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Allah bütün günahları bağışlar." to "Zümer Suresi, 53. ayet",
        "Namaz, insanı hayasızlıktan ve kötülükten alıkoyar." to "Ankebut Suresi, 45. ayet",
        "Ey iman edenler! Şeytanın adımlarını izlemeyin." to "Nur Suresi, 21. ayet",
        "Allah hiç kimseye gücünün yetmeyeceği bir yük yüklemez." to "Bakara Suresi, 286. ayet",
        "Mü'minler gerçekten kurtuluşa ermiştir... Onlar ki iffetlerini korurlar." to "Mü'minun Suresi, 1 ve 5. ayetler",
        "Allah, gözlerin hain bakışını ve kalplerin gizlediğini bilir." to "Ğafir Suresi, 19. ayet",
        "Kim Allah'tan sakınırsa, Allah ona bir çıkış yolu açar." to "Talak Suresi, 2. ayet",
        "Kim bana dilini ve iffetini koruyacağına söz verirse, ben de ona cenneti garanti ederim." to "Hadis, Buhari",
        "Haya imandandır." to "Hadis, Buhari ve Müslim",
        "Gözlerin zinası bakmaktır." to "Hadis, Buhari ve Müslim",
        "Ey gençler! Evlenmeye gücü yeten evlensin; çünkü evlilik gözü haramdan ve iffeti daha iyi korur. Gücü yetmeyen oruç tutsun, oruç onun için bir kalkandır." to "Hadis, Buhari ve Müslim",
        "Nerede olursan ol Allah'tan sakın. Kötülüğün arkasından bir iyilik yap ki onu silsin." to "Hadis, Tirmizi",
        "Allah'ın rahmeti, gazabını geçmiştir." to "Hadis-i Kudsi, Buhari ve Müslim"
    )

    val tips = listOf(
        "Kalk, abdest al ve iki rekat namaz kıl. Dürtü 10-15 dakikada geçer.",
        "Telefonu bırak, odadan çık ve 5 dakika yürü.",
        "Şimdi 20 şınav çek, enerjini oraya ver.",
        "Bir arkadaşını ara ya da ailenle biraz konuş. Yalnız kalma.",
        "Bir sayfa Kur'an oku ya da sevdiğin bir sureyi dinle.",
        "Gece telefonu yatağa alma; şarja odanın dışında tak.",
        "Daha önce düştüysen bile vazgeçme. Tövbe et ve kaldığın yerden devam et.",
        "Bu his geçici, ama bugün kazandığın irade kalıcı."
    )

    fun random() = reminders.random()
}
