package com.litiaina.android.sdk.retrofit

import com.google.gson.JsonObject
import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.MessageData
import com.litiaina.android.sdk.data.ResponseResult
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.data.UserData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface UserApiService {

    @POST("messages/create_message")
    suspend fun createMessage(
        @Header("authorization") token: String,
        @Body messageData: MessageData
    ): Response<Void>

    @GET("messages/search/{field}/{filter}")
    suspend fun getMessagesByFieldAndFilter(
        @Header("authorization") token: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): List<MessageData>

    @GET("custom/{collection}")
    suspend fun getCustomBsonDocuments(
        @Header("authorization") token: String,
        @Path("collection") collection: String,
    ): Response<JsonObject>

    @POST("custom/create/{collection}")
    suspend fun createCustomData(
        @Header("authorization") token: String,
        @Path("collection") collection: String,
        @Body document: Map<String, Any>
    ): Response<Void>

    @GET("custom/search/{collection}/{field}/{filter}")
    suspend fun getCustomBsonFilteredDocument(
        @Header("authorization") token: String,
        @Path("collection") collection: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): List<Map<String, Any>>

    @DELETE("custom/delete/{collection}/{field}/{filter}")
    suspend fun deleteCustomBsonDocument(
        @Header("authorization") token: String,
        @Path("collection") collection: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): Response<JsonObject>

}