package me.adobot.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import me.adobot.R


class OverlayService : Service() {

    private val li by lazy { getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater }
    private val view by lazy { li.inflate(R.layout.overlay_1, null) }
    private val windowManager by lazy { applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        foo()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun foo() {


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
        /*view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()


            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_BUTTON_PRESS -> v.performClick()
                    MotionEvent.ACTION_UP -> return true
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, params)
                        return true
                    }
                }
                return false
            }
        })*/

    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(view)
    }


}
