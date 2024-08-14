package com.example.mireandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mireandroid.service.CallListenerService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, CallListenerService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
