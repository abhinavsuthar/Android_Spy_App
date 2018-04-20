package me.adobot.tasks

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


class TakeScreenShot : Thread() {


    fun screenShot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun run() {
        super.run()


    }
}