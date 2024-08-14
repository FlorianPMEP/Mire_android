package com.example.mireandroid.ui

import android.os.Bundle
import android.telecom.Call
import android.telecom.InCallService
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mireandroid.R

class CallActivity : AppCompatActivity() {

    object CallManager {
        var currentCall: Call? = null
    }


    private var currentCall: Call? = null
    private lateinit var callInfoTextView: TextView
    private lateinit var hangUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        callInfoTextView = findViewById(R.id.call_info_text_view)
        hangUpButton = findViewById(R.id.hang_up_button)

        // Récupère l'appel en cours à partir de CallManager
        currentCall = CallManager.currentCall

        currentCall?.let { call ->
            updateCallInfo(call)
            hangUpButton.setOnClickListener {
                call.disconnect()
                finish() // Terminer l'activité lorsque l'appel est raccroché
            }
        }
    }

    private fun updateCallInfo(call: Call) {
        callInfoTextView.text = "Appel en cours avec : ${call.details.handle.schemeSpecificPart}"
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                super.onStateChanged(call, state)
                if (state == Call.STATE_DISCONNECTED) {
                    finish() // Fermer l'activité lorsque l'appel est terminé
                }
            }
        })
    }
}
