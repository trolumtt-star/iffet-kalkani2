package com.iffet.kalkan

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/** Cihaz yöneticisi olunca uygulama direkt silinemez. Kaldırma ekranını da kalkan kilitliyken engeller. */
class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        "Kardeşim emin misin? Bu kararı dürtü anında verme. Uygulamadan 10 dk bekleme başlatman gerekiyor."
}
