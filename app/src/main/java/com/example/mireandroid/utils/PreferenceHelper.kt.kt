package com.example.mireandroid.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mireandroid.api.AuthUser
import com.example.mireandroid.api.User
import com.example.mireandroid.api.dataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferenceHelper {
    private const val PREFERENCES_FILE = "com.example.mireandroid.PREFERENCES"
    private const val USER_KEY = "USER"
    private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")

    suspend fun saveJwtToken(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    fun getJwtToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[JWT_TOKEN_KEY]
        }
    }

    fun saveUser(context: Context, user: AuthUser) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_KEY, Gson().toJson(user))
        editor.apply()
    }

    fun getUser(context: Context): User? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val userJson = sharedPreferences.getString(USER_KEY, null) ?: return null
        return Gson().fromJson(userJson, User::class.java)
    }

    suspend fun deleteJwtToken(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }

    fun deleteUser(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USER_KEY)
        editor.apply()
    }
}
