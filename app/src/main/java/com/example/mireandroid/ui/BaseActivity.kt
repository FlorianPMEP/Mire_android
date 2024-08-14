package com.example.mireandroid.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mireandroid.BuildConfig
import com.example.mireandroid.R
import com.example.mireandroid.api.Dossier
import com.example.mireandroid.api.Email
import com.example.mireandroid.api.Organisation
import com.example.mireandroid.api.Person
import com.example.mireandroid.api.PersonsService
import com.example.mireandroid.api.Phone
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.User
import com.example.mireandroid.api.UsersService
import com.example.mireandroid.service.CallListenerService
import com.example.mireandroid.state.UserViewModel
import com.example.mireandroid.utils.PreferenceHelper
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var leftDrawer: NavigationView
    private lateinit var rightDrawer: NavigationView

    private val REQUEST_READ_PHONE_STATE = 1
    private val REQUEST_READ_CALL_LOG = 2
    private val REQUEST_POST_NOTIFICATIONS = 3
    private val REQUEST_SET_DEFAULT_DIALER = 1234

    private lateinit var searchBar: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: SearchResultsAdapter

    private var attemptCount = 0
    private val maxAttempts = 3


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, CallListenerService::class.java)
        startForegroundService(serviceIntent)
        setContentView(R.layout.activity_base)

        requestSetDefaultDialer()

        initializeUI()
        requestPermissions()

        searchResultsRecyclerView = findViewById(R.id.recycler_view_search_results)
        searchResultsAdapter = SearchResultsAdapter()
        searchResultsRecyclerView.adapter = searchResultsAdapter
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)

        searchBar = findViewById(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        searchEverywhere(query)
                        adjustScrollViewHeight(true)
                        searchResultsRecyclerView.visibility = View.VISIBLE
                    } else {
                        searchResultsRecyclerView.visibility = View.GONE
                        adjustScrollViewHeight(false)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        adjustScrollViewHeight(false)
    }

    private fun searchEverywhere(query: String) {
        Log.d("searchEverywhere", "Recherche : $query")
        val personsService = RetrofitClient.create(this).create(PersonsService::class.java)
        Log.d("searchEverywhere", "Service créé")
        val call = personsService.searchEverywhere(query)
        Log.d("searchEverywhere", "Requête envoyée")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d("searchEverywhere", "Réponse JSON brute : $responseBody")
                    try {
                        val gson = Gson()
                        val typeDossier = object : TypeToken<List<List<Dossier>>>() {}.type
                        val typePerson = object : TypeToken<List<List<Person>>>() {}.type
                        val typeOrganisation = object : TypeToken<List<List<Organisation>>>() {}.type
                        val typeEmail = object : TypeToken<List<List<Email>>>() {}.type
                        val typePhone = object : TypeToken<List<List<Phone>>>() {}.type
                        val searchResultsDossier: List<List<Dossier>> = gson.fromJson(responseBody, typeDossier)
                        val searchResultsPerson: List<List<Person>> = gson.fromJson(responseBody, typePerson)
                        val searchResultsOrganisation: List<List<Organisation>> = gson.fromJson(responseBody, typeOrganisation)
                        val searchResultsEmail: List<List<Email>> = gson.fromJson(responseBody, typeEmail)
                        val searchResultsPhone: List<List<Phone>> = gson.fromJson(responseBody, typePhone)

                        fun <T> flattenList(nestedList: List<List<T>>): List<T> {
                            val flattenedList = mutableListOf<T>()
                            nestedList.forEach { innerList ->
                                flattenedList.addAll(innerList)
                            }
                            return flattenedList
                        }

                        val flattenedDossier = flattenList(searchResultsDossier)
                        val flattenedPerson = flattenList(searchResultsPerson)
                        val flattenedOrganisation = flattenList(searchResultsOrganisation)

                        val allSearchResults = mutableListOf<Any>()
                        allSearchResults.addAll(flattenedDossier)
                        allSearchResults.addAll(flattenedPerson)
                        allSearchResults.addAll(flattenedOrganisation)

                        Log.d("searchEverywhere", "Résultats 1 : $allSearchResults")
                        searchResultsRecyclerView.visibility = View.VISIBLE
                        searchResultsAdapter.updateResults(allSearchResults)
                        Log.d("searchEverywhere", "C'est passé")
                    } catch (e: JsonSyntaxException) {
                        Log.e("searchEverywhere", "Erreur de syntaxe JSON", e)
                    }
                } else {
                    Log.e("searchEverywhere", "Erreur lors de la recherche : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("searchEverywhere", "Échec de la requête", t)
            }
        })
    }

    private fun adjustScrollViewHeight(isSearchActive: Boolean) {
        val scrollView: NestedScrollView = findViewById(R.id.scroll_view)
        val layoutParams = scrollView.layoutParams as LinearLayout.LayoutParams

        layoutParams.height = if (isSearchActive) {
            100
        } else {
            LinearLayout.LayoutParams.MATCH_PARENT
        }
        scrollView.layoutParams = layoutParams
    }

    protected fun getMyInfos() {
        val usersService = RetrofitClient.create(this).create(UsersService::class.java)
        val call = usersService.getMyUserInfo()

        call.enqueue(object : Callback<User> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("authUser", user.toString())
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

    private fun initializeUI() {
        // Initialisation des drawers et de la toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
        leftDrawer = findViewById(R.id.left_drawer)
        rightDrawer = findViewById(R.id.right_drawer)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                setupToolbarButtons(this, toolbar, user)
                setupLeftDrawer(this, user)
            }
        }
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_READ_PHONE_STATE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Les permissions sont nécessaires pour le bon fonctionnement de l'application.", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission for notifications granted
            } else {
                // Permission for notifications denied
                Toast.makeText(this, "Les notifications ne seront pas affichées sans permission.", Toast.LENGTH_LONG).show()
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1001)
        }
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

        val profilePictureUrl = BuildConfig.BASE_URL_STORAGE + user.profile_picture.file_path
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
            val intent = Intent(this@BaseActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val profileImage: ImageView = rightDrawer.findViewById(R.id.profile_image)
        val profileName: TextView = rightDrawer.findViewById(R.id.profile_name)
        val profileRole: TextView = rightDrawer.findViewById(R.id.profile_role)
        val profileEmail: TextView = rightDrawer.findViewById(R.id.profile_email)
        val profilePhone: TextView = rightDrawer.findViewById(R.id.profile_phone)
        val profileSignature: TextView = rightDrawer.findViewById(R.id.profile_signature)
        profileName.text = user.name
        profileRole.text = user.role.role
        profileEmail.text = user.email
        profilePhone.text = user.phone ?: "Aucune numéro de téléphone renseigné"
        profileSignature.text = user.role_signature ?: "Aucune signature renseignée"

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

    private fun setupLeftDrawer(context: Context, user: User) {
        leftDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@BaseActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true // Indicate that the item was handled
                }
                R.id.nav_contacts -> {
                    val intent = Intent(this@BaseActivity, ContactsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true // Indicate that the item was handled
                }
                R.id.nav_organisations -> {
                    val intent = Intent(this@BaseActivity, OrgantisationsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true // Indicate that the item was handled
                }
                R.id.nav_dossiers -> {
                    val intent = Intent(this@BaseActivity, DossiersActivity::class.java)
                    startActivity(intent)
                    finish()
                    true // Indicate that the item was handled
                }
                R.id.nav_dialer -> {
                    val intent = Intent(this@BaseActivity, YourDialerActivity::class.java)
                    startActivity(intent)
                    finish()
                    true // Indicate that the item was handled
                }
                // Handle other menu items here if needed
                else -> false // Indicate the item wasn't handled
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

    private fun requestSetDefaultDialer() {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
        val roleManager = getSystemService(Context.ROLE_SERVICE) as android.app.role.RoleManager

        val isDefaultDialer = packageName == telecomManager.defaultDialerPackage
        Log.d("isDefaultDialer", isDefaultDialer.toString())

        if (!isDefaultDialer) {
            Log.d("isDefaultDialer", "L'application n'est pas définie comme gestionnaire d'appels par défaut")
            if (attemptCount < maxAttempts) {
                attemptCount++
                Log.d("isDefaultDialer", "Tentative n°$attemptCount")
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                roleManagerLauncher.launch(intent)
                Log.d("isDefaultDialer", "Activity démarrée")
            } else {
                showAlertToUser()
                Log.d("isDefaultDialer", "Nombre maximal de tentatives atteint")
            }
        } else {
            Toast.makeText(this, "L'application est déjà définie comme gestionnaire d'appels par défaut", Toast.LENGTH_LONG).show()
        }
    }

    private val roleManagerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "L'application a été définie comme gestionnaire d'appels par défaut", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Définition du gestionnaire d'appels par défaut annulée", Toast.LENGTH_LONG).show()
            if (attemptCount < maxAttempts) {
                showAlertToUser()
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SET_DEFAULT_DIALER) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "L'application a été définie comme gestionnaire d'appels par défaut", Toast.LENGTH_LONG).show()
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Définition du gestionnaire d'appels par défaut annulée", Toast.LENGTH_LONG).show()
                    if (attemptCount < maxAttempts) {
                        showAlertToUser()
                    }
                }
                else -> {
                    Toast.makeText(this, "Échec de la tentative de définition comme gestionnaire d'appels par défaut", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAlertToUser() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission requise")
        builder.setMessage("Pour fonctionner correctement, cette application doit être définie comme gestionnaire d'appels par défaut. Voulez-vous réessayer ?")
        builder.setPositiveButton("Réessayer") { _, _ ->
            requestSetDefaultDialer()
        }
        builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
