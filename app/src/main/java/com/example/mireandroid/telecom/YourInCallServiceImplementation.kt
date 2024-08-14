package com.example.mireandroid.telecom

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.widget.Toast
import com.example.mireandroid.ui.CallActivity

class YourInCallServiceImplementation : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallActivity.CallManager.currentCall = call

        val intent = Intent(this, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallActivity.CallManager.currentCall = null
    }
}

