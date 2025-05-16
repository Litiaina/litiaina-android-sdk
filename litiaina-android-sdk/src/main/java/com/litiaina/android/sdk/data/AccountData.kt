package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

data class AccountData(
    val uid: String,
    val name: String,
    val email: String,
    @SerializedName("profile_picture") val profilePicture: String,
    @SerializedName("access_level") val accessLevel: Int,
    @SerializedName("allocated_storage") val allocatedStorage: Long
)
