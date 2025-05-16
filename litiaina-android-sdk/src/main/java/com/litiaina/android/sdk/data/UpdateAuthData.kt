package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

internal data class UpdateAuthData(
    @SerializedName("current_email") val currentEmail: String? = null,
    @SerializedName("current_password") val currentPassword: String? = null,
    @SerializedName("new_name") val newName: String? = null,
    @SerializedName("new_profile_picture") val profilePicture: String? = null,
)
