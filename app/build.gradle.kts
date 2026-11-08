plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.iffet.kalkan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iffet.kalkan"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }

    // Sabit imza: her derleme aynı anahtarla imzalanır, güncellemeler eskisinin üstüne kurulur.
    signingConfigs {
        create("kalkan") {
            storeFile = file("kalkan.keystore")
            storePassword = "kalkan123"
            keyAlias = "kalkan"
            keyPassword = "kalkan123"
        }
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("kalkan") }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("kalkan")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Ek kütüphane yok: sadece Android'in kendi API'leri kullanılıyor.
}
