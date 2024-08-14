package com.example.mireandroid.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

@Parcelize
data class Person(
    val id: Long,
    val civilite_id: Long?,
    val name: String,
    val first_name: String,
    val apport: String?,
    val apport_note: String?,
    val linkedin: String?,
    val profile_id: Long,
    val acquisition_id: Long,
    val entreprise_name_cnq: String?,
    val linkedin_user_id: String?,
    val created_at: String,
    val updated_at: String,
    val created_by: Long,
    val updated_by: Long,
    val blacklist: Double,
    val pivot: @RawValue Map<String, Any>
) : Parcelable {
    override fun hashCode(): Int {
        // Assurez-vous que les attributs ne sont pas nulls
        val nameHash = name?.hashCode() ?: 0
        val firstNameHash = first_name?.hashCode() ?: 0
        return id.hashCode() * 31 + nameHash * 31 + firstNameHash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (id != other.id) return false
        if (name != other.name) return false
        if (first_name != other.first_name) return false

        return true
    }
}

@Parcelize
data class Phone(
    val id: Long,
    val phone: String,
    val is_favorite: Double,
    val created_at: String,
    val updated_at: String,
    val persons: List<Person>
) : Parcelable

@Parcelize
data class SearchResponse(
    val data: List<Phone>
) : Parcelable

data class SearchEverywhereResponse(
    val type: String,
    val data: Any
)

interface PersonsService {
    @Headers("Content-Type: application/json")
    @GET("/api/phones")
    fun searchPhones(
        @Query("modalsearch") searchedPhone: String,
        @Query("nbresults") nbResults: Int = 10
    ): Call<SearchResponse>

    @Headers("Content-Type: application/json")
    @GET("searchEverywhere")
    fun searchEverywhere(@Query("modalsearch") modalsearch: String): Call<ResponseBody>
}
