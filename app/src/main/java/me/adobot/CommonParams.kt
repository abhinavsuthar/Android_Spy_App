package me.adobot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager


class CommonParams(ctx: Context) {

    //private val prefs: SharedPreferences = ctx.getSharedPreferences(Constants.PACKAGE_NAME, Context.MODE_PRIVATE)
    val server: String
    val uid: String
    val sdk: String
    val version: String
    val phone: String
    val provider: String
    val device: String

    init {
        server = Constants.DEVELOPMENT_SERVER   //prefs.getString(Constants.PREF_SERVER_URL_FIELD, Constants.DEVELOPMENT_SERVER)
        uid = Settings.Secure.getString(ctx.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        sdk = Integer.valueOf(Build.VERSION.SDK_INT)!!.toString()
        version = Build.VERSION.RELEASE
        val telephonyManager = ctx.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        provider = telephonyManager.networkOperatorName
        phone = if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) telephonyManager.line1Number else "<unknown>"
        device = android.os.Build.MODEL
    }
}