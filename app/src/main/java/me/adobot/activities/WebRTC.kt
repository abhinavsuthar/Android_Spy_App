package me.adobot.activities

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import me.adobot.R
import android.R.attr.data
import android.app.Activity


class WebRTC : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_rtc)

        screenShot()
    }

    private fun screenShot() {
        val mgr= getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mgr.createScreenCaptureIntent(), 555)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 555) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }
}
