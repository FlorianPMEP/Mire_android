package com.example.mireandroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mireandroid.R
import com.example.mireandroid.service.OverlayService

class OverlayActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val acceptButton = findViewById<Button>(R.id.accept_call)
        val rejectButton = findViewById<Button>(R.id.reject_call)

        acceptButton.setOnClickListener {
            // Répondre à l'appel
            answerCall()
        }

        rejectButton.setOnClickListener {
            // Rejeter l'appel
            rejectCall()
        }

        // Vérifiez et demandez la permission de superposition si nécessaire, puis démarrez le service
        checkOverlayPermissionAndStartService()
    }

    private fun checkOverlayPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
            } else {
                startOverlayService()
            }
        } else {
            startOverlayService()
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            // Vérifiez à nouveau la permission et démarrez le service si elle est accordée
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startOverlayService()
                } else {
                    // Permission non accordée, gérer l'erreur ici
                }
            }
        }
    }

    private fun answerCall() {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            telecomManager.acceptRingingCall()
        }
    }

    private fun rejectCall() {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            telecomManager.endCall()
        }
    }
}
