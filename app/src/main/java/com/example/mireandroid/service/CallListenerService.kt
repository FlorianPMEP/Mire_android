package com.example.mireandroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mireandroid.R

class CallListenerService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Créer un canal de notification pour Android O et plus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CallServiceChannel",
                "Call Listener Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Créer une notification pour le service
        val notification: Notification = NotificationCompat.Builder(this, "CallServiceChannel")
            .setContentTitle("Call Listener Service")
            .setContentText("Monitoring incoming calls...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        // Lancer le service en mode foreground avec la notification
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Le service peut redémarrer automatiquement si le système le tue
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
