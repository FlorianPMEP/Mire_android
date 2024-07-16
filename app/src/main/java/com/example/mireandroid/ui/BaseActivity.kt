package com.example.mireandroid.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.mireandroid.R
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.User
import com.example.mireandroid.api.UsersService
import com.example.mireandroid.state.UserViewModel
import com.example.mireandroid.utils.PreferenceHelper
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    lateinit var drawerLayout: DrawerLayout
    lateinit var leftDrawer: NavigationView
    lateinit var rightDrawer: NavigationView

    private val REQUEST_READ_PHONE_STATE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // Initialisation des drawers et de la toolbar
        initializeUI()

        // Demander les permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG),
                REQUEST_READ_PHONE_STATE)
        }
    }

    private fun initializeUI() {
        // Initialisation des drawers et de la toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
        leftDrawer = findViewById(R.id.left_drawer)
        rightDrawer = findViewById(R.id.right_drawer)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        userViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                setupToolbarButtons(this, toolbar, user)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }

    private fun getMyInfos() {
        val usersService = RetrofitClient.create(this).create(UsersService::class.java)
        val call = usersService.getMyUserInfo()

        call.enqueue(object : Callback<User> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("authUser", user.toString())

                    val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
                    val name = user?.name?.split(" ")?.get(0)
                    welcomeTextView.text = "$name, bienvenue sur Mire"
                    welcomeTextView.setTextColor(Color.parseColor("#bbbbbb"))

                    // Update the ViewModel
                    userViewModel.setUser(user)
                } else {
                    Log.e("authUser", "Erreur lors de la récupération de l'utilisateur")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("authUser", "Échec de la requête", t)
            }
        })
    }

    protected fun setLayout(layoutResID: Int) {
        val contentFrame: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(layoutResID, contentFrame)
    }

    protected fun setupToolbarButtons(context: Context, toolbarView: View, user: User) {
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
            drawerLayout.openDrawer(GravityCompat.START)
        }

        rightMenuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        centerButton.setOnClickListener {
            Toast.makeText(context, "Bouton central cliqué", Toast.LENGTH_SHORT).show()
        }

        val profileImage: ImageView = rightDrawer.findViewById(R.id.profile_image)
        val profileName: TextView = rightDrawer.findViewById(R.id.profile_name)
        profileName.text = user.name

        Glide.with(context)
            .load(profilePictureUrl)
            .placeholder(R.drawable.ic_menu_right)
            .error(R.drawable.ic_menu_right)
            .circleCrop()
            .into(profileImage)

        val logoutButton: Button = rightDrawer.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                PreferenceHelper.deleteJwtToken(context)
                PreferenceHelper.deleteUser(context)

                CoroutineScope(Dispatchers.Main).launch {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    Toast.makeText(context, "Déconnecté", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            drawerLayout.isDrawerOpen(GravityCompat.END) -> drawerLayout.closeDrawer(GravityCompat.END)
            else -> super.onBackPressed()
        }
    }
}
