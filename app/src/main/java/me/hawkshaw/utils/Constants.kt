package me.hawkshaw.utils

import android.Manifest
import io.socket.client.Socket

object Constants {

    var socket: Socket? = null
    val DEVELOPMENT_SERVER = "http://kiran.fr.openode.io/"

    val PACKAGE_NAME = "me.adobot"
    val UPDATE_PKG_FILE_NAME = "update.apk"
    val SMS_FORWARDER_SIGNATURE = "AdoBot SMS Forwarder"
    val PREF_SERVER_URL_FIELD = "serverUrl"
    const val tag = "suthar"


    val PERMISSIONS = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

}