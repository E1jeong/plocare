package com.senplo.plocare

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import com.senplo.plocare.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PloCareApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        initKoin {
            androidLogger()
            androidContext(this@PloCareApp)
        }

        // Initialize Kakao Map SDK
        val kakaoAppKey = getString(R.string.kakao_app_key)
        if (kakaoAppKey != "YOUR_KAKAO_NATIVE_APP_KEY") {
            KakaoMapSdk.init(this, kakaoAppKey)
        }
    }
}
