package me.hawkshaw.tasks

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import io.socket.client.Socket
import org.json.JSONObject
import android.location.Criteria
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import org.jetbrains.anko.runOnUiThread


/**
 * Created by Abhinav_Suthar on 17-03-2018.
 */

class LocationMonitor(private val ctx: Context, private val socket: Socket) : BaseTask(ctx) {

    companion object {
        var locationUpdates = true
    }


    private fun getLocation() {
        //Implement location
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val bestProvider = locationManager.getBestProvider(Criteria(), false)

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            uploadLocation(lastLocation)

            Log.d(tag, lastLocation?.toString()?:"null")

            ctx.runOnUiThread {

                locationManager.requestLocationUpdates(bestProvider, 400, 1F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {

                        uploadLocation(location)
                        if (!locationUpdates) locationManager.removeUpdates(this)
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                    override fun onProviderEnabled(provider: String?) = Unit

                    override fun onProviderDisabled(provider: String?) = Unit

                })
            }

        } else requestPermissions()
    }

    fun getLastLocation(): JSONObject? {
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val bestProvider = locationManager.getBestProvider(Criteria(), false)

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val location = JSONObject()
            location.put("lat", lastLocation?.latitude)
            location.put("lon", lastLocation?.longitude)
            return location
        }
        return null
    }

    private fun uploadLocation(location: Location?) {

        val obj = JSONObject()
        obj.put("lat", location?.latitude)
        obj.put("lon", location?.longitude)

        val data = JSONObject()
        data.put("location", obj.toString())
        data.put("dataType", "location")

        socket.emit("usrData", data)
    }

    override fun run() {
        super.run()
        getLocation()
    }

}