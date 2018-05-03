package me.adobot.activities

import android.app.Activity
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import kotlinx.android.synthetic.main.activity_web_rtc.*
import me.adobot.R
import me.adobot.services.OverlayService


class WebRTC : Activity() {

    private val mgr by lazy { getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager }
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_rtc)

        screenCast()
    }

    private fun screenCast() {
            startActivityForResult(mgr.createScreenCaptureIntent(), 555)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 555 && resultCode == RESULT_OK) {
            postFun(resultCode, data)
        }

        stopService(Intent(this, OverlayService::class.java))
    }

    private fun postFun(resultCode: Int, data: Intent?) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val surface = surfaceView.holder.surface

        val mMediaProjection = mgr.getMediaProjection(resultCode, data)
        val virtualDisplay: VirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                surfaceView.width, surfaceView.height, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null)

    }

}
