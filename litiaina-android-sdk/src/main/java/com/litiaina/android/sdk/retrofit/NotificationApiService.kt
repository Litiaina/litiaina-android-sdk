package com.litiaina.android.sdk.retrofit

import com.google.gson.JsonObject
import com.litiaina.android.sdk.data.MultiplePushRequest
import com.litiaina.android.sdk.data.NotificationPushRequest
import retrofit2.Response
import retrofit2.http.Body
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

}