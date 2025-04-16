package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

internal data class SignUpData(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("access_level") val accessLevel: String? = null
)
