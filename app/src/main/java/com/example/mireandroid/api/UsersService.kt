package com.example.mireandroid.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import java.util.Date

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val google_access_token_json: String? = null,
    val google_id: Int = 0,
    val is_activated: Boolean = false,
    val info: String? = null,
    val phone: String? = null,
    val role: @RawValue Any? = null,
    val role_id: Int? = null,
    val role_signature: String? = null,
    val favourite_color: String? = null,
    val picture_id: Int? = null,
    val profile_picture: @RawValue ProfilePicture,
    val created_at: Date = Date(),
    val dossiers: List<Dossier> = emptyList(),
    val dossiers_chauds: List<Dossier> = emptyList(),
    val new_notifications: @RawValue List<Any> = emptyList(),
    val notifications_send: @RawValue List<Any> = emptyList(),
    val old_notifications: @RawValue List<Any> = emptyList(),
    val todo_notifications: @RawValue List<Any> = emptyList(),
    val todos: @RawValue List<Any> = emptyList(),
    val refresh_token: String? = null,
) : Parcelable

data class ProfilePicture(
    val id: Int,
    val file_path: String,
    val file_name: String,
    val creator_id: Int,
    val trombinoscope: Int,
    val created_at: Date = Date(),
    val updated_at: Date = Date(),
)

interface UsersService {
    @Headers("Content-Type: application/json")
    @GET("myinfos")
    fun getMyUserInfo(): Call<User>
}
