package com.example.mireandroid.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import java.util.Date

@Parcelize
data class Dossier(
    val campaign_checkboxes_values: @RawValue Any,
    val campaign_status_id: Int,
    val candidatures: @RawValue List<Any>? = null,
    val consultant: @RawValue Any,
    val consultant_id: Int,
    val consultants_chauds: @RawValue List<Any>? = null,
    val created_at: Date,
    val current_campaign_status: @RawValue Any,
    val current_status: @RawValue Any,
    val dossier_checkboxes_values: @RawValue Any,
    val dossier_name: String,
    val files: @RawValue List<Any>,
    val front_fees: Int? = null,
    val honoraires: Int,
    val lost_candidats: @RawValue List<Any>,
    val max_valorisation: Int,
    val min_valorisation: Int,
    val notes: @RawValue List<Any>,
    val organisation: @RawValue Any,
    val organisation_id: Int,
    val persons: @RawValue List<Any>,
    val pivot: @RawValue Any,
    val potential_candidats: @RawValue List<Any>? = null,
    val prix_vente: Int? = null,
    val prix_vente_final: Int? = null,
    val stand_by: Int,
    val status_id: Int,
    val suivi_campaign_status: @RawValue List<Any>,
    val suivi_status: @RawValue List<Any>,
    val updated_at: Date,
    val valorisateur: @RawValue Any,
    val valorisateur_id: Int? = null,
    val id: Int
) : Parcelable