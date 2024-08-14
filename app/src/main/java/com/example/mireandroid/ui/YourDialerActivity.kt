package com.example.mireandroid.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mireandroid.R

class YourDialerActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var callButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_dialer)

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text)
        callButton = findViewById(R.id.call_button)

        callButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString()
            if (phoneNumber.isNotEmpty()) {
                initiateCall(phoneNumber)
            } else {
                Toast.makeText(this, "Veuillez entrer un numéro de téléphone", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initiateCall(phoneNumber: String) {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager

        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_CALL, uri)
        if (telecomManager.defaultDialerPackage == packageName) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Définissez cette application comme gestionnaire d'appels par défaut pour passer des appels", Toast.LENGTH_SHORT).show()
        }
    }
}
