package me.hawkshaw.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager


class CommonParams(ctx: Context) {

    val server: String = Constants.DEVELOPMENT_SERVER
    val uid: String = "HW-" + Settings.Secure.getString(ctx.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
    val sdk: String = Integer.valueOf(Build.VERSION.SDK_INT).toString()
    val version: String = Build.VERSION.RELEASE
    val serial: String = Build.SERIAL ?: "<unknown>"
    val phone: String
    val provider: String
    val device: String

    init {
        val telephonyManager = ctx.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        provider = telephonyManager.networkOperatorName
        phone = if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) telephonyManager.line1Number
                ?: "<empty>" else "<unknown>"
        device = android.os.Build.MODEL
    }
}