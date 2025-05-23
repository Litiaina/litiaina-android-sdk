package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

internal data class SignUpData(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("profile_picture") val profilePicture: String,
)
