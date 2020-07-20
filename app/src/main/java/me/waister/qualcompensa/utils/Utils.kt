package me.waister.qualcompensa.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import android.webkit.URLUtil
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import me.waister.qualcompensa.BuildConfig
import me.waister.qualcompensa.services.MyFirebaseMessagingService

fun Context.storeAppLink(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun String?.stringToInt(): Int {
    if (this != null && this != "null") {
        val number = this.replace("[^\\d]".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.isValidUrl(): Boolean {
    return this != null && this.isNotEmpty() && URLUtil.isValidUrl(this)
}

fun Context?.getThumbUrl(image: String?, width: Int = 220, height: Int = 0, quality: Int = 85): String {
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
            val canvas = Canvas(output!!)

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
        println("\n--------------- REQUEST_REQUEST_START - ${request.path}\n")
        println(request)
        println("\n--------------- REQUEST_REQUEST_END - ${request.path}\n")
        println("\n--------------- RESPONSE_RESPONSE_START - ${request.path}\n")
        println(response)
        println("\n--------------- RESPONSE_RESPONSE_END - ${request.path}\n")
        println("\n--------------- RESULT_RESULT_START - ${request.path}\n")
        println(result)
        println("\n--------------- RESULT_RESULT_END - ${request.path}\n")
    }
}

fun debugLog(message: String) {
    if (BuildConfig.DEBUG)
        Log.i(MyFirebaseMessagingService.TAG, message)
}
