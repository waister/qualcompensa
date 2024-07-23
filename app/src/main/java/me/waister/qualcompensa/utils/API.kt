package me.waister.qualcompensa.utils

import org.json.JSONException
import org.json.JSONObject

const val APP_HOST = "https://maggapps.com/"

const val API_ROUTE_IDENTIFY = "/identify"

const val API_TOKEN = "token"
const val API_ANDROID = "android"
const val API_IDENTIFIER = "identifier"
const val API_VERSION = "version"
const val API_PLATFORM = "platform"
const val API_DEBUG = "debug"
const val API_V = "api_v"
const val API_SUCCESS = "success"

fun String?.getValidJSONObject(): JSONObject? {
    if (!this.isNullOrEmpty() && this != "null") {
        try {
            return JSONObject(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getBooleanVal(tag: String, default: Boolean = false): Boolean {
    if (this != null && has(tag)) {
        try {
            return getBoolean(tag)
        } catch (_: JSONException) {
        }
    }
    return default
}