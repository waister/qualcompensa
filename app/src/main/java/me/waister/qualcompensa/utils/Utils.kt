package me.waister.qualcompensa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.LinearLayout
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import me.waister.qualcompensa.BuildConfig
import java.util.UUID

fun Context.storeAppLink(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun String?.stringToInt(): Int {
    try {
        if (this != null && this != "null") {
            val number = this.replace("\\D".toRegex(), "")
            if (number.isNotEmpty())
                return number.toInt()
        }
    } catch (_: Exception) {
    }
    return 0
}

fun String?.isValidUrl(): Boolean {
    return !this.isNullOrEmpty() && URLUtil.isValidUrl(this)
}

fun Context?.getThumbUrl(
    image: String?,
    width: Int = 220,
    height: Int = 0,
    quality: Int = 85
): String {
    if (this != null && image != null && !image.contains("http") && image.contains("/uploads/")) {
        return APP_HOST + "thumb?src=$image&w=$width&h=$height&q=$quality"
    }

    return image.getApiImage()
}

fun String?.getApiImage(): String {
    if (this != null) {
        if (!contains("http") && contains("/uploads/")) {
            val path = APP_HOST.removeSuffix("/") + this

            if (path.isValidUrl()) {
                return path
            }
        }

        return this
    }

    return ""
}

fun Bitmap?.getCircleCroppedBitmap(): Bitmap? {
    var output: Bitmap? = null
    val bitmap = this

    if (bitmap != null) {
        try {
            output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            if (bitmap.width < bitmap.height) {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.width / 2).toFloat(), paint
                )
            } else {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.height / 2).toFloat(), paint
                )
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return output
}

fun printFuelLog(request: Request, response: Response, result: Result<String, FuelError>) {
    if (BuildConfig.DEBUG) {
        println("\n--------------- REQUEST_REQUEST_START - ${request.url}\n")
        println(request)
        println("\n--------------- REQUEST_REQUEST_END - ${request.url}\n")
        println("\n--------------- RESPONSE_RESPONSE_START - ${request.url}\n")
        println(response)
        println("\n--------------- RESPONSE_RESPONSE_END - ${request.url}\n")
        println("\n--------------- RESULT_RESULT_START - ${request.url}\n")
        println(result)
        println("\n--------------- RESULT_RESULT_END - ${request.url}\n")
    }
}

fun Context?.loadAdBanner(adViewContainer: LinearLayout?, adUnitId: String, adSize: AdSize? = null, collapsible: Boolean = false) {
    val logTag = "LOAD_ADMOB_BANNER"

    if (this == null || adUnitId.isEmpty() || adViewContainer == null) {
        appLog(logTag, "loadAdMobBanner() falied | $this | $adUnitId")
        return
    }

    appLog(logTag, "adUnitId: $adUnitId")

    val adView = AdView(this)
    adViewContainer.addView(adView)

    adView.adUnitId = if (isDebug()) "ca-app-pub-3940256099942544/6300978111" else adUnitId

    adView.setAdSize(adSize ?: getAdSize(adViewContainer))

    val extras = Bundle()
    if (collapsible) {
        extras.putString("collapsible", "bottom")
        extras.putString("collapsible_request_id", UUID.randomUUID().toString())
    }

    val adRequest = AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        .build()

    adView.loadAd(adRequest)

    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            appLog(logTag, "onAdLoaded()")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            appLog(logTag, "onAdFailedToLoad(): ${error.message}")
        }

        override fun onAdOpened() {
            super.onAdOpened()
            appLog(logTag, "onAdOpened()")
        }

        override fun onAdClosed() {
            super.onAdClosed()
            appLog(logTag, "onAdClosed()")
        }
    }

    appLog(logTag, "ENDS")
}

fun Context.getAdSize(adViewContainer: LinearLayout): AdSize {
    var adWidthPixels = adViewContainer.width.toFloat()
    if (adWidthPixels == 0f)
        adWidthPixels = displayWidth().toFloat()

    val density = resources.displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}

fun Context?.displayWidth(): Int {
    return if (this != null) resources.displayMetrics.widthPixels else 0
}

fun appLog(tag: String, msg: String) {
    if (BuildConfig.DEBUG)
        Log.i("MAGGAPPS_LOG", "➡➡➡ $tag: $msg")
}

fun isDebug() = BuildConfig.DEBUG

fun View?.isVisible(isVisible: Boolean) {
    this?.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View?.hide() {
    this.isVisible(false)
}

fun String?.isNumeric(): Boolean {
    if (this == null) return false
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return this.matches(regex)
}

fun String?.isNotNumeric(): Boolean = !this.isNumeric()
