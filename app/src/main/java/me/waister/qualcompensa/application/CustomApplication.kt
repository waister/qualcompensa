package me.waister.qualcompensa.application

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.ads.MobileAds
import com.orhanobut.hawk.Hawk
import me.waister.qualcompensa.BuildConfig
import me.waister.qualcompensa.utils.*

class CustomApplication : Application() {

    var isCheckUpdatesNeeded: Boolean = true

    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()

        MobileAds.initialize(this) {}

        AppOpenManager(this)

        FuelManager.instance.basePath = "${APP_HOST}api/qualcompensa"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
                API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
                API_VERSION to BuildConfig.VERSION_CODE,
                API_PLATFORM to API_ANDROID,
                API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
                API_V to 8
        )
    }

}