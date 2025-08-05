package com.litiaina.android.sdk.retrofit

import com.google.gson.JsonObject
import com.litiaina.android.sdk.data.DeviceTokenRegistryRequest
import com.litiaina.android.sdk.data.DeviceTokenRemovalRequest
import com.litiaina.android.sdk.data.MultiplePushRequest
import com.litiaina.android.sdk.data.NotificationPushRequest
import com.litiaina.android.sdk.data.NotificationTokenRequest
import com.litiaina.android.sdk.data.NotificationTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST

internal interface NotificationApiService {

    @POST("notification/push")
    suspend fun sendSingleMessage(
        @Header("authorization") token: String,
        @Body request: NotificationPushRequest
    ): Response<JsonObject>

    @POST("notification/push/multiple")
    suspend fun sendMultipleMessage(
        @Header("authorization") token: String,
        @Body request: MultiplePushRequest
    ): Response<JsonObject>

    @POST("auth/device/token/register")
    suspend fun addDeviceTokenToAccount(
        @Header("authorization") token: String,
        @Body request: DeviceTokenRegistryRequest
    ): Response<JsonObject>

    @HTTP(method = "DELETE", path = "auth/device/token/remove", hasBody = true)
    suspend fun removeDeviceTokenToAccount(
        @Header("authorization") token: String,
        @Body request: DeviceTokenRemovalRequest
    ): Response<JsonObject>

    @POST("/notification/retrieve/token")
    suspend fun getNotificationToken(
        @Header("authorization") token: String,
        @Body request: NotificationTokenRequest
    ): Response<NotificationTokenResponse>
}
