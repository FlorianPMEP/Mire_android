package com.example.mireandroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.PersonsService
import com.example.mireandroid.api.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d("IncomingCallReceiver", "Incoming call from: $incomingNumber")

                incomingNumber?.let {
                    context?.let { ctx ->
                        searchPhone(ctx, it)
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
                    searchResponse?.data?.forEach { phone ->
                        phone.persons.forEach { person ->
                            val fullName = "${person.first_name} ${person.name}"
                            Log.d("IncomingCallReceiver", "Person: $fullName")
                            Toast.makeText(context, "Person found: $fullName", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("IncomingCallReceiver", "Search failed with response: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("IncomingCallReceiver", "Search failed", t)
            }
        })
    }
}
