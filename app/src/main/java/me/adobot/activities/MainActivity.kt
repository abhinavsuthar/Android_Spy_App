package me.adobot.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import me.adobot.Constants
import me.adobot.R

class MainActivity : BaseActivity() {

    private val prefs by lazy { this.getSharedPreferences("com.android.adobot", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, WebRTC::class.java)
        startActivity(intent)

        val url = prefs.getString("serverUrl", Constants.DEVELOPMENT_SERVER)

        if (url == null || url == Constants.DEVELOPMENT_SERVER || url == "") {
            edit_text_server_url.setText(url)
            getNewServerURL()
        } else {
            done()
        }

    }

    private fun getNewServerURL() {

        text_instruction.text = "Set your server address. Make sure it has \"http://\" or \"https://\" in front of the domain name or IP address and has NO slash \"/\" at the end of the URL.\n\nExamples:\n\nhttps://adobot.herokuapp.com\nhttp://123.123.12.123"

        btn_set_server.setOnClickListener({
            val url: String = edit_text_server_url.text.toString()
            setServerUrl(url) //or you can verify server url by creating additional dialog
        })
    }


    private fun setServerUrl(url: String) {
        prefs.edit().putString("serverUrl", url).apply()
        if (hasPermissions()) done()
        else requestPermissions()
    }
}
