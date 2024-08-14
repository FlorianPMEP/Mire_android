package com.example.mireandroid.api

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import androidx.datastore.preferences.preferencesDataStore
import com.example.mireandroid.BuildConfig
import com.google.gson.GsonBuilder

val Context.dataStore by preferencesDataStore("settings")

object RetrofitClient {
    private const val BASE_URL = BuildConfig.BASE_URL

    fun create(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val tokenInterceptor = TokenInterceptor(context.dataStore)

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
    fun createPersonsService(context: Context): PersonsService {
        return create(context).create(PersonsService::class.java)
    }
}
