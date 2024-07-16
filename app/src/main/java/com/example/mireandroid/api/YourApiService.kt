package com.example.mireandroid.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class TokenRequest(val token: String)
data class TokenResponse(val token: String, val user: AuthUser)
data class AuthUser(val id: String, val name: String, val email: String)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("auth/googleMobile")
    fun getCustomToken(@Body request: TokenRequest): Call<TokenResponse>
}
