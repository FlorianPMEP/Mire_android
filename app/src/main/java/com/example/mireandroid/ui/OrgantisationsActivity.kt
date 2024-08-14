package com.example.mireandroid.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.example.mireandroid.R
import com.example.mireandroid.state.UserViewModel
import com.example.mireandroid.utils.setupToolbarButtons

class OrgantisationsActivity : BaseActivity() {

    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.activity_organisations)

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
