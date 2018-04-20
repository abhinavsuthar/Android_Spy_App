package me.adobot.models


data class Sms(val id: String, val threadId: String, val address: String, val name: String, val body: String, val date: String, val type: String)
data class CallLog(val number: String, val name: String, val date: String, val duration: String, val type: String, val new: String)
data class Contact(val id: String, val name: String, val phoneNo: ArrayList<String>)
data class Image(val name: String, val path: String, val date: String, val bucket_name: String)