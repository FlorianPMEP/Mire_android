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
) : Parcelable

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
interface PersonsService {
    @Headers("Content-Type: application/json")
    @GET("/api/phones")
    fun searchPhones(
        @Query("modalsearch") searchedPhone: String,
        @Query("nbresults") nbResults: Int = 10
    ): Call<SearchResponse>
}
