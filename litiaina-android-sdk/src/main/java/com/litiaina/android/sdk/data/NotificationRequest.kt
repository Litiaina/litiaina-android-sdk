package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

data class NotificationPushRequest(
    val token: String,
    val title: String,
    val body: String
)

data class MultiplePushRequest(
    val tokens: List<String>,
    val title: String,
    val body: String
)

data class DeviceTokenRegistryRequest(
    val email: String,
    val password: String,
    val otp: String? = null,
    val token: String,
    val platform: String? = null,
    @SerializedName("device_info") val deviceInfo: String? = null,
)

data class DeviceTokenRemovalRequest(
    val email: String,
    val password: String,
    val otp: String? = null,
    val platform: String? = null,
    @SerializedName("device_info") val deviceInfo: String? = null,
)

data class NotificationTokenRequest(
    val uid: String
)

data class NotificationTokenResponse(
    val result: List<TokenData>
)

data class TokenData(
    @SerializedName("_id")
    val id: IdWrapper,

    val uid: String,
    val token: String,
    val platform: String,

    @SerializedName("device_info")
    val deviceInfo: String,

    @SerializedName("created_at")
    val createdAt: DateWrapper,

    @SerializedName("updated_at")
    val updatedAt: DateWrapper
)

data class IdWrapper(
    @SerializedName("\$oid")
    val oid: String
)

data class DateWrapper(
    @SerializedName("\$date")
    val date: NumberLongWrapper
)

data class NumberLongWrapper(
    @SerializedName("\$numberLong")
    val numberLong: String
)