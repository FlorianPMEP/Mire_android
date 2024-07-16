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

class HomeActivity : BaseActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.activity_home)

        // Toolbar est déjà gérée par BaseActivity
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Observe the user data and set up the toolbar when it changes
        userViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                setupToolbarButtons(this, toolbar, user)
            }
            user?.let {
                updateDossiers(it)
            }
        })

        getMyInfos()
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

    private fun updateDossiers(user: User) {
        val prospectDossiers = user.dossiers.filter { it.status_id in 1..8 } ?: emptyList()
        val mandatsDossiers = user.dossiers.filter { it.status_id in 9..16 } ?: emptyList()
        val dossiersChauds = user.dossiers_chauds ?: emptyList()

        val container: LinearLayout = findViewById(R.id.dossiersContainer)

        val parentContainer = createParentContainer()

        addDossierSection(parentContainer, "Vos dossiers chauds", dossiersChauds, "#FFA36F", "#FFFFFF")
        addDossierSection(parentContainer, "Prospects", prospectDossiers, "#FFFFFF", "#4285F4")
        addDossierSection(parentContainer, "Mandats", mandatsDossiers, "#FFFFFF", "#4285F4")

        container.addView(parentContainer)
    }

    @SuppressLint("SetTextI18n")
    private fun createParentContainer(): LinearLayout {
        val parentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 60)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 50, 0, 10)
            layoutParams = params
            background = ShapeDrawable().apply {
                shape = RoundRectShape(
                    floatArrayOf(16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f),
                    null, null)
                paint.color = Color.parseColor("#f0f0f0")
            }
        }

        val titleTextView = TextView(this).apply {
            text = "Vos dossiers"
            textSize = 20f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 30)
        }

        parentContainer.addView(titleTextView)

        return parentContainer
    }

    private fun addDossierSection(container: LinearLayout, title: String, dossiers: List<Dossier>, titleBgColor: String, itemBgColor: String) {
        val sectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(titleBgColor))
            setPadding(40, 25, 40, 40)
            elevation = 4f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 10)
            layoutParams = params
            background = ShapeDrawable().apply {
                shape = RoundRectShape(
                    floatArrayOf(16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f),
                    null, null)
                paint.color = Color.parseColor(titleBgColor)
            }
        }

        val arrowTextView = TextView(this).apply {
            text = "▼"
            textSize = 18f
            setTextColor(Color.BLACK)
        }

        val dossiersContainer = GridLayout(this).apply {
            columnCount = 2
            visibility = View.VISIBLE
        }

        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setOnClickListener {
                val isVisible = dossiersContainer.visibility == View.VISIBLE
                dossiersContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
                arrowTextView.text = if (isVisible) "▲" else "▼"
            }
        }

        val titleTextView = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        titleContainer.addView(titleTextView)
        titleContainer.addView(arrowTextView)
        sectionLayout.addView(titleContainer)

        dossiers.forEach { dossier ->
            val dossierItem = TextView(this).apply {
                text = dossier.dossier_name
                textSize = 16f
                setBackgroundColor(Color.parseColor(itemBgColor))
                setTextColor(if (itemBgColor == "#FFFFFF") Color.BLACK else Color.WHITE)
                setPadding(40, 40, 40, 40)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(20, 20, 20, 20)
                }
                layoutParams = params
                background = ShapeDrawable().apply {
                    shape = RoundRectShape(
                        floatArrayOf(16f, 16f, 16f, 16f, 16f, 16f, 16f, 16f),
                        null, null)
                    paint.color = Color.parseColor(itemBgColor)
                    maxHeight = 140
                }
            }
            dossiersContainer.addView(dossierItem)
        }

        sectionLayout.addView(dossiersContainer)
        container.addView(sectionLayout)
    }
}
