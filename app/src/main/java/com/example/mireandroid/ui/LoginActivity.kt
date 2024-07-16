package com.example.mireandroid.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mireandroid.BuildConfig
import com.example.mireandroid.R
import com.example.mireandroid.api.ApiService
import com.example.mireandroid.api.RetrofitClient
import com.example.mireandroid.api.TokenRequest
import com.example.mireandroid.api.TokenResponse
import com.example.mireandroid.utils.PreferenceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.SignInButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate: started")
        val googleWebKey: String = BuildConfig.GOOGLE_WEB_API_KEY
        Log.d(TAG, "Google Web API Key: $googleWebKey")
        val googleSecretClient: String = BuildConfig.GOOGLE_CLIENT_SECRET

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(googleWebKey)
            .requestServerAuthCode(googleWebKey)
            .requestScopes(Scope("https://www.googleapis.com/auth/userinfo.profile"))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d(TAG, "onCreate: GoogleSignInClient configured")

        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            Log.d(TAG, "onCreate: Sign in button clicked")
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        Log.d(TAG, "signIn: starting sign-in intent")
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d(TAG, "onActivityResult: task=$task")
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "handleSignInResult: account=$account")
            if (account != null) {
                val serverAuthCode = account.serverAuthCode
                Log.d(TAG, "handleSignInResult: signIn successful, serverAuthCode=$serverAuthCode")
                if (serverAuthCode != null) {
                    Log.d(TAG, "handleSignInResult: Server auth code is not null: $serverAuthCode")
                    fetchAccessToken(serverAuthCode)
                } else {
                    Log.e(TAG, "handleSignInResult: Server auth code is null")
                }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "handleSignInResult: signIn failed, ${e.message}, ${e.statusCode}", e)
            Toast.makeText(this, "Sign In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAccessToken(authCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val requestBody = ("code=$authCode&"
                        + "client_id=${BuildConfig.GOOGLE_WEB_API_KEY}&"
                        + "client_secret=${BuildConfig.GOOGLE_CLIENT_SECRET}&"
                        + "redirect_uri=&"
                        + "grant_type=authorization_code")
                Log.d(TAG, "fetchAccessToken: Request body: $requestBody")
                val request = Request.Builder()
                    .url("https://oauth2.googleapis.com/token")
                    .post(requestBody.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull()))
                    .build()
                Log.d(TAG, "fetchAccessToken: Request: $request")
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    Log.d(TAG, responseBody)
                }
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val accessToken = json.getString("access_token")
                    Log.d(TAG, "fetchAccessToken: Access token: $accessToken")
                    getCustomToken(accessToken)
                } else {
                    Log.e(TAG, "fetchAccessToken: Failed to fetch access token")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAccessToken: Exception", e)
            }
        }
    }

    private fun getCustomToken(googleAccessToken: String) {
        val apiService = RetrofitClient.create(this).create(ApiService::class.java)
        val call = apiService.getCustomToken(TokenRequest(googleAccessToken))

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            PreferenceHelper.saveJwtToken(this@LoginActivity, tokenResponse.token)
                            PreferenceHelper.saveUser(this@LoginActivity, tokenResponse.user)
                        }
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this@LoginActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "getCustomToken: Server returned an error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@LoginActivity, "Connexion échouée", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.e(TAG, "getCustomToken: Network request failed", t)
                Toast.makeText(this@LoginActivity, "Connexion échouée", Toast.LENGTH_SHORT).show()
            }
        })
    }


    companion object {
        private const val TAG = "LoginActivity"
    }
}
