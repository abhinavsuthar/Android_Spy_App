package me.adobot.activities

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import me.adobot.R
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.preference.PreferenceManager
import android.view.View
import kotlinx.android.synthetic.main.activity_web_rtc.*
import android.util.DisplayMetrics
import android.util.Log
import com.google.gson.Gson
import me.adobot.Constants


class WebRTC : Activity() {

    private val mgr by lazy { getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager }
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_rtc)

//        val bitmap: Bitmap? = takeScreenShotMethod2(imageView.rootView)
//        imageView.setImageBitmap(bitmap)
//
//        Handler().postDelayed({ finish() }, 5000)
        screenShot()
    }

    private fun screenShot() {
        val data = restoreIntent()
        if (data == null)
            startActivityForResult(mgr.createScreenCaptureIntent(), 555)
        else {
            postFun(resultCode, data)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 555 && resultCode == RESULT_OK) {
            saveIntent(resultCode, data)
            postFun(resultCode, data)
        }
    }

    private fun postFun(resultCode: Int, data: Intent?) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val surface = surfaceView.holder.surface

        val mMediaProjection = mgr.getMediaProjection(resultCode, data)
        val virtualDisplay: VirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null)

        virtualDisplay

    }


    private fun saveIntent(resultCode: Int, intent: Intent?) {
        sp.edit().putString("SOME_KEY", intent?.toURI()).apply()
        sp.edit().putString("SOME_KEY2", resultCode.toString()).apply()

        //val x: String = Gson().toJson(intent)

        //Log.d(Constants.TAG, "Json: $x")
        //Log.d(Constants.TAG, "Intent: $intent")
    }

    private var resultCode: Int = 0
    private fun restoreIntent(): Intent? {
        val uri = sp.getString("SOME_KEY", "")
        val resultCodeT = sp.getString("SOME_KEY2", "0")
        resultCode = resultCodeT.toInt()
        if (uri == null || uri == "") return null
        return Intent.getIntent(uri)
    }

    private fun takeScreenShotMethod2(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

}
