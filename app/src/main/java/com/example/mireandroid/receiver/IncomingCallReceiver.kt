package com.example.mireandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import com.example.mireandroid.ui.OverlayActivity

class IncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d("com.example.mireandroid.receiver.IncomingCallReceiver", "Appel entrant : $incomingNumber")

                // Allumer l'écran
                val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp::WakeLockTag"
                )
                wakeLock.acquire(3000) // Garder l'écran allumé pendant 3 secondes

                // Lancer l'activité de gestion d'appels
                val callIntent = Intent(context, OverlayActivity::class.java)
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(callIntent)
            }
        }
    }
}
