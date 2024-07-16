package com.example.mireandroid.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mireandroid.R
import com.example.mireandroid.api.User
import com.example.mireandroid.ui.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun setupToolbarButtons(context: Context, toolbarView: View, user: User) {
    val leftMenuButton: ImageButton = toolbarView.findViewById(R.id.left_menu_button)
    val rightMenuButton: ImageButton = toolbarView.findViewById(R.id.right_menu_button)
    val centerButton: ImageButton = toolbarView.findViewById(R.id.toolbar_title)

    Log.d("User", user.toString())

    val profilePictureUrl = "https://api.dev-rose-pmep.fr/storage/" + user.profile_picture.file_path
    Log.d("User", profilePictureUrl)

    Glide.with(context)
        .load(profilePictureUrl)
        .placeholder(R.drawable.ic_menu_right)
        .error(R.drawable.ic_menu_right)
        .circleCrop()
        .into(rightMenuButton)

    leftMenuButton.setOnClickListener {
        // Logic for the left menu
        Toast.makeText(context, "Menu gauche cliqué", Toast.LENGTH_SHORT).show()
    }

    rightMenuButton.setOnClickListener {
        CoroutineScope(Dispatchers.IO).launch {
            PreferenceHelper.deleteJwtToken(context)
            PreferenceHelper.deleteUser(context)

            // Start the login activity on the main thread
            CoroutineScope(Dispatchers.Main).launch {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                Toast.makeText(context, "Déconnecté", Toast.LENGTH_SHORT).show()
            }
        }
    }

    centerButton.setOnClickListener {
        // Logic for the center button
        Toast.makeText(context, "Bouton central cliqué", Toast.LENGTH_SHORT).show()
    }
}
