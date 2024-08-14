package com.example.mireandroid.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
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
    val role: @RawValue Role,
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

data class Role(
    val id: Int,
    val role: String,
    val created_at: Date = Date(),
    val updated_at: Date = Date(),
)

data class SearchResult(
    val id: Int,
    val name: String,
    val first_name: String,
    val civilite_id: Int,
    val profile_id: Int,
    val phones: List<Phone>,
    val emails: List<Email>,
    val profile: Profile
)

data class Email(
    val id: Int,
    val email: String,
    val is_favorite: Int,
    val created_at: String,
    val updated_at: String,
    val pivot: Pivot
)

data class Pivot(
    val person_id: Int,
    val phone_id: Int? = null,
    val email_id: Int? = null
)

data class Profile(
    val id: Int,
    val profile_name: String,
    val sort: Any? // Adjust type if sort has a specific structure
)


interface UsersService {
    @Headers("Content-Type: application/json")
    @GET("myinfos")
    fun getMyUserInfo(): Call<User>

}
