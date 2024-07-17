package com.example.mireandroid

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IncomingCallReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "incoming_call_channel"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (!incomingNumber.isNullOrEmpty()) {
                    // Remplace +33 par 0 si le numéro commence par +33
                    val modifiedNumber = if (incomingNumber.startsWith("+33")) {
                        "0" + incomingNumber.substring(3)
                    } else {
                        incomingNumber
                    }

                    Log.d("IncomingCallReceiver", "Incoming call from: $modifiedNumber")

                    context?.let { ctx ->
                        searchPhone(ctx, modifiedNumber)
                    }
                }
            }
        }
    }

    private fun searchPhone(context: Context, searchedPhone: String) {
        val personsService = RetrofitClient.createPersonsService(context)
        val call = personsService.searchPhones(searchedPhone)
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()

                    val firstPhone = searchResponse?.data?.firstOrNull()
                    if (firstPhone != null) {
                        val firstPerson = firstPhone.persons.firstOrNull()
                        if (firstPerson != null) {
                            val fullName = "${firstPerson.first_name} ${firstPerson.name}"
                            sendNotification(context, fullName)
                        } else {
                            Toast.makeText(context, "No persons found for the phone", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No phone data found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Phone not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("IncomingCallReceiver", "Search failed", t)
            }
        })
    }


    private fun sendNotification(context: Context, fullName: String) {
        Toast.makeText(context, "$fullName est en train de vous appeler", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "$fullName est en train de vous appeler", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "$fullName est en train de vous appeler", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "$fullName est en train de vous appeler", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("IncomingCallReceiver", "Notification permission not granted")
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming Call Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for incoming call notifications"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.mire_small_white_logo)
            .setContentTitle(fullName)
            .setContentText("est en train de vous appeler")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setChannelId(CHANNEL_ID)

        notificationManager.notify(1, builder.build())

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            notificationManager.cancel(1)
        }, 15000)

    }
}
