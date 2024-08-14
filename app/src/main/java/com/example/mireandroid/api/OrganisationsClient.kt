package com.example.mireandroid.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Organisation(
    val id: Long,
    val organisation_name: String,
    val adress: String,
) : Parcelable

