package me.waister.qualcompensa.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import me.waister.qualcompensa.application.CustomApplication
import java.util.Date

class AppOpenManager(private var application: CustomApplication) : DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallback: AppOpenAd.AppOpenAdLoadCallback
    private var currentActivity: Activity? = null
    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private const val AD_UNIT_ID = "ca-app-pub-6521704558504566/6559082028"
    }

    init {
        this.application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        showAdIfAvailable()

        super.onStart(owner)
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            appLog(LOG_TAG, "Will show ad")

            appOpenAd?.show(currentActivity!!)
        } else {
            appLog(LOG_TAG, "Can not show ad")

            fetchAd()
        }
    }

    private fun fetchAd() {
        if (isAdAvailable()) {
            appLog(LOG_TAG, "No ad loaded or expired")

            return
        }

        val adUnitId = if (isDebug()) "ca-app-pub-3940256099942544/3419835294" else AD_UNIT_ID

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appLog(LOG_TAG, "AppOpenAd was loaded")

                appOpenAd = ad
                loadTime = Date().time

                super.onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                appLog(LOG_TAG, "Ad failed to load: ${loadAdError.message}")

                appOpenAd = null
                loadTime = 0

                super.onAdFailedToLoad(loadAdError)
            }
        }

        appLog(LOG_TAG, "Ad unit id: $adUnitId")

        AppOpenAd.load(application, adUnitId, AdRequest.Builder().build(), loadCallback)
    }

    private fun isAdAvailable(): Boolean {
        if (appOpenAd != null) {
            val validateInHours = 4
            val dateDifference = (Date()).time - this.loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return (dateDifference < (numMilliSecondsPerHour * validateInHours))
        }

        return false
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }


}