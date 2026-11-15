package com.iffet.kalkan

/**
 * Ayetler (Arapçası + meali) ve hadisler (anlam olarak). Meal anlamı aktarır, kelimesi kelimesine değildir.
 * Kalkan ekranında karışık sırayla gösterilir: sadece iffet değil; tövbe, umut, gelecek, başarı,
 * sabır ve Allah'a sığınma ayetleri de var.
 */
data class Verse(val theme: String, val arabic: String?, val meal: String, val source: String)

object Content {
    private const val IFFET = "İffet"
    private const val SIGINMA = "Allah'a sığınma"
    private const val TOVBE = "Tövbe ve rahmet"
    private const val UMUT = "Umut ve gelecek"
    private const val BASARI = "Gayret ve başarı"
    private const val SABIR = "Sabır ve güç"

    val verses = listOf(
        // --- Allah'a sığınma ---
        Verse(SIGINMA, "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "Kovulmuş şeytanın şerrinden Allah'a sığınırım.", "Eûzü besmele"),
        Verse(SIGINMA,
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            "De ki: İnsanların Rabbine, insanların Melikine, insanların İlahına sığınırım; sinsi vesvesecinin şerrinden. O ki insanların göğüslerine vesvese verir; cinlerden de insanlardan da.",
            "Nas Suresi, 1-6"),
        Verse(SIGINMA,
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            "De ki: Sabahın Rabbine sığınırım; yarattığı şeylerin şerrinden, karanlığı çöktüğünde gecenin şerrinden, düğümlere üfleyenlerin şerrinden ve haset ettiğinde hasetçinin şerrinden.",
            "Felak Suresi, 1-5"),
        Verse(SIGINMA,
            "وَإِمَّا يَنْزَغَنَّكَ مِنَ الشَّيْطَانِ نَزْغٌ فَاسْتَعِذْ بِاللَّهِ ۚ إِنَّهُ سَمِيعٌ عَلِيمٌ",
            "Eğer şeytandan bir kışkırtma seni dürterse hemen Allah'a sığın. Şüphesiz O her şeyi işitir, her şeyi bilir.",
            "A'raf Suresi, 200"),
        Verse(SIGINMA,
            "إِنَّ الَّذِينَ اتَّقَوْا إِذَا مَسَّهُمْ طَائِفٌ مِنَ الشَّيْطَانِ تَذَكَّرُوا فَإِذَا هُمْ مُبْصِرُونَ",
            "Allah'tan sakınanlara şeytandan bir vesvese dokunduğunda düşünüp Allah'ı hatırlarlar, bir de bakarsın ki gözleri açılmıştır.",
            "A'raf Suresi, 201"),
        Verse(SIGINMA,
            "وَقُلْ رَبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ ۝ وَأَعُوذُ بِكَ رَبِّ أَنْ يَحْضُرُونِ",
            "De ki: Rabbim! Şeytanların kışkırtmalarından sana sığınırım. Onların yanımda bulunmalarından da sana sığınırım Rabbim!",
            "Mü'minun Suresi, 97-98"),
        Verse(SIGINMA,
            "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
            "De ki: O Allah birdir. Allah her şeyden müstağnidir, her şey O'na muhtaçtır. Doğurmamış ve doğmamıştır. Hiçbir şey O'na denk değildir.",
            "İhlas Suresi, 1-4"),

        // --- İffet ---
        Verse(IFFET,
            "قُلْ لِلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ وَيَحْفَظُوا فُرُوجَهُمْ ۚ ذَٰلِكَ أَزْكَىٰ لَهُمْ ۗ إِنَّ اللَّهَ خَبِيرٌ بِمَا يَصْنَعُونَ",
            "Mü'min erkeklere söyle: Gözlerini haramdan sakınsınlar ve iffetlerini korusunlar. Bu onlar için daha temizdir. Allah yaptıklarından hakkıyla haberdardır.",
            "Nur Suresi, 30"),
        Verse(IFFET, "وَلَا تَقْرَبُوا الزِّنَا ۖ إِنَّهُ كَانَ فَاحِشَةً وَسَاءَ سَبِيلًا",
            "Zinaya yaklaşmayın bile. Çünkü o bir hayasızlıktır ve çok kötü bir yoldur.", "İsra Suresi, 32"),
        Verse(IFFET, "وَالَّذِينَ هُمْ لِفُرُوجِهِمْ حَافِظُونَ",
            "(Kurtuluşa eren mü'minler) iffetlerini koruyanlardır.", "Mü'minun Suresi, 5"),
        Verse(IFFET, "قَالَ رَبِّ السِّجْنُ أَحَبُّ إِلَيَّ مِمَّا يَدْعُونَنِي إِلَيْهِ",
            "Yusuf dedi ki: Rabbim! Zindan bana, bunların beni çağırdığı şeyden daha sevimlidir.",
            "Yusuf Suresi, 33"),
        Verse(IFFET, null,
            "Kulak, göz ve kalp; bunların her biri yaptıklarından sorumludur.", "İsra Suresi, 36"),
        Verse(IFFET, null,
            "Allah gözlerin hain bakışını ve kalplerin gizlediğini bilir.", "Ğafir Suresi, 19"),

        // --- Tövbe ve rahmet ---
        Verse(TOVBE,
            "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
            "De ki: Ey nefislerine karşı aşırı giden kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Allah bütün günahları bağışlar. Çünkü O çok bağışlayandır, çok merhametlidir.",
            "Zümer Suresi, 53"),
        Verse(TOVBE,
            "وَالَّذِينَ إِذَا فَعَلُوا فَاحِشَةً أَوْ ظَلَمُوا أَنْفُسَهُمْ ذَكَرُوا اللَّهَ فَاسْتَغْفَرُوا لِذُنُوبِهِمْ",
            "Onlar bir hayasızlık yaptıklarında ya da kendilerine zulmettiklerinde Allah'ı hatırlayıp hemen günahlarının bağışlanmasını isterler.",
            "Al-i İmran Suresi, 135"),
        Verse(TOVBE,
            "إِلَّا مَنْ تَابَ وَآمَنَ وَعَمِلَ عَمَلًا صَالِحًا فَأُولَٰئِكَ يُبَدِّلُ اللَّهُ سَيِّئَاتِهِمْ حَسَنَاتٍ",
            "Ancak tövbe eden, iman edip salih amel işleyenler başka. Allah onların kötülüklerini iyiliklere çevirir.",
            "Furkan Suresi, 70"),
        Verse(TOVBE, "إِنَّ الْحَسَنَاتِ يُذْهِبْنَ السَّيِّئَاتِ",
            "Şüphesiz iyilikler kötülükleri giderir.", "Hud Suresi, 114"),

        // --- Umut ve gelecek ---
        Verse(UMUT,
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ فَإِذَا فَرَغْتَ فَانْصَبْ ۝ وَإِلَىٰ رَبِّكَ فَارْغَبْ",
            "Şüphesiz zorlukla beraber bir kolaylık vardır. Evet, zorlukla beraber bir kolaylık vardır. Bir işi bitirince başka bir işe koyul ve yalnız Rabbine yönel.",
            "İnşirah Suresi, 5-8"),
        Verse(UMUT, "وَلَلْآخِرَةُ خَيْرٌ لَكَ مِنَ الْأُولَىٰ ۝ وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            "Senin için sonrası öncekinden elbette daha hayırlıdır. Rabbin sana verecek, sen de razı olacaksın.",
            "Duha Suresi, 4-5"),
        Verse(UMUT,
            "وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ ۚ وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            "Kim Allah'tan sakınırsa Allah ona bir çıkış yolu açar ve onu hiç ummadığı yerden rızıklandırır. Kim Allah'a tevekkül ederse O ona yeter.",
            "Talak Suresi, 2-3"),
        Verse(UMUT, "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "Bilin ki kalpler ancak Allah'ı anmakla huzur bulur.", "Ra'd Suresi, 28"),
        Verse(UMUT, "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنْتُمُ الْأَعْلَوْنَ إِنْ كُنْتُمْ مُؤْمِنِينَ",
            "Gevşemeyin, üzülmeyin. Eğer gerçekten iman ediyorsanız üstün olan sizsiniz.", "Al-i İmran Suresi, 139"),

        // --- Gayret ve başarı ---
        Verse(BASARI,
            "وَالَّذِينَ جَاهَدُوا فِينَا لَنَهْدِيَنَّهُمْ سُبُلَنَا ۚ وَإِنَّ اللَّهَ لَمَعَ الْمُحْسِنِينَ",
            "Bizim uğrumuzda mücadele edenleri elbette yollarımıza ulaştıracağız. Şüphesiz Allah iyilik yapanlarla beraberdir.",
            "Ankebut Suresi, 69"),
        Verse(BASARI, "إِنَّ اللَّهَ لَا يُغَيِّرُ مَا بِقَوْمٍ حَتَّىٰ يُغَيِّرُوا مَا بِأَنْفُسِهِمْ",
            "Bir toplum kendini değiştirmedikçe Allah onların durumunu değiştirmez.", "Ra'd Suresi, 11"),
        Verse(BASARI, "وَأَنْ لَيْسَ لِلْإِنْسَانِ إِلَّا مَا سَعَىٰ",
            "İnsan için ancak çalıştığının karşılığı vardır.", "Necm Suresi, 39"),
        Verse(BASARI, null,
            "Namaz insanı hayasızlıktan ve kötülükten alıkoyar.", "Ankebut Suresi, 45"),

        // --- Sabır ve güç ---
        Verse(SABIR,
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "Ey iman edenler! Sabır ve namazla Allah'tan yardım isteyin. Şüphesiz Allah sabredenlerle beraberdir.",
            "Bakara Suresi, 153"),
        Verse(SABIR, "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "Allah hiç kimseye gücünün yetmeyeceği bir yük yüklemez.", "Bakara Suresi, 286"),

        // --- Hadisler ---
        Verse(IFFET, null, "Kim bana dilini ve iffetini koruyacağına söz verirse, ben de ona cenneti garanti ederim.", "Hadis, Buhari"),
        Verse(IFFET, null, "Haya imandandır.", "Hadis, Buhari ve Müslim"),
        Verse(IFFET, null, "Ey gençler! Evlenmeye gücü yeten evlensin; çünkü evlilik gözü haramdan, iffeti de daha iyi korur. Gücü yetmeyen oruç tutsun, oruç onun için bir kalkandır.", "Hadis, Buhari ve Müslim"),
        Verse(TOVBE, null, "Nerede olursan ol Allah'tan sakın. Kötülüğün arkasından bir iyilik yap ki onu silsin.", "Hadis, Tirmizi"),
        Verse(TOVBE, null, "Allah'ın rahmeti gazabını geçmiştir.", "Hadis-i Kudsi, Buhari ve Müslim"),
        Verse(BASARI, null, "Güçlü mü'min, Allah katında zayıf mü'minden daha hayırlı ve daha sevimlidir. Sana fayda verecek şeye sarıl, Allah'tan yardım iste ve aciz olma.", "Hadis, Müslim")
    )

    val tips = listOf(
        "Kalk, abdest al ve iki rekat namaz kıl. Dürtü 10-15 dakikada geçer.",
        "Telefonu bırak, odadan çık ve 5 dakika yürü.",
        "Şimdi 20 şınav çek, enerjini oraya ver.",
        "Bir arkadaşını ara ya da ailenle biraz konuş. Yalnız kalma.",
        "Bir sayfa Kur'an oku ya da sevdiğin bir sureyi dinle.",
        "Gece telefonu yatağa alma; şarja odanın dışında tak.",
        "Daha önce düştüysen bile vazgeçme. Tövbe et ve kaldığın yerden devam et.",
        "Bu his geçici, ama bugün kazandığın irade kalıcı.",
        "Yüzünü soğuk suyla yıka, derin nefes al ve 33 kere \"Estağfirullah\" de."
    )

    /** Her seferinde farklı temalardan karışık bir sıra. İlk ayet hep sığınma ayetlerinden. */
    fun shuffledSequence(): List<Verse> {
        val first = verses.filter { it.theme == SIGINMA }.random()
        return listOf(first) + (verses - first).shuffled()
    }

    fun random() = verses.random()
}
