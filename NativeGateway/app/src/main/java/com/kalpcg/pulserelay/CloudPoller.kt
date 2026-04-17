package com.kalpcg.pulserelay

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CloudPoller {
    var isRunning = false
    private var job: Job? = null
    
    // Default config. In a real app, bind this to your Preferences!
    var backendUrl = "https://smsauth-it1p.onrender.com" // Pointing to live Render cloud backend

    fun start(context: Context) {
        if (isRunning) return
        isRunning = true
        Log.d("CloudPoller", "Starting Cloud Poller to $backendUrl")
        
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    pollPendingMessages()
                } catch (e: Exception) {
                    Log.e("CloudPoller", "Polling error: ${e.message}")
                }
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        Log.d("CloudPoller", "Stopped Cloud Poller")
    }

    private fun pollPendingMessages() {
        Log.d("CloudPoller", "Checking for pending messages...")
        val url = URL("$backendUrl/api/messages/pending")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val array = JSONArray(response)
            
            for (i in 0 until array.length()) {
                val msgObj = array.getJSONObject(i)
                val id = msgObj.getString("id")
                val phone = msgObj.getString("phoneNumber")
                val text = msgObj.getString("message")
                
                Log.d("CloudPoller", "Got message to send: $phone | $text")
                sendRealSms(id, phone, text)
            }
        }
        conn.disconnect()
    }

    private fun sendRealSms(id: String, phone: String, text: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(text)
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            
            reportStatus(id, "sent", null)
        } catch (e: Exception) {
            Log.e("CloudPoller", "Failed to send SMS: ${e.message}")
            reportStatus(id, "failed", e.message)
        }
    }

    private fun reportStatus(id: String, status: String, error: String?) {
        try {
            val url = URL("$backendUrl/api/messages/status")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            
            val json = JSONObject().apply {
                put("id", id)
                put("status", status)
                if (error != null) put("error", error)
            }
            
            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
            conn.responseCode // execute
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("CloudPoller", "Failed to report status: ${e.message}")
        }
    }
}
