package com.example.mireandroid.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.example.mireandroid.R
import com.example.mireandroid.api.Dossier
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.User
import com.example.mireandroid.api.UsersService
import com.example.mireandroid.state.UserViewModel
import com.example.mireandroid.utils.setupToolbarButtons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactsActivity : BaseActivity() {

    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.activity_contacts)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        userViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                setupToolbarButtons(this, toolbar, user)
            }
        })
        getMyInfos()
    }

}
