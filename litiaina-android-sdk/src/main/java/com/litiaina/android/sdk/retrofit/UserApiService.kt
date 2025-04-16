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

    @GET("users")
    fun getUsers(
        @Header("X-API-KEY") apiKey: String,
    ): Call<List<UserData>>
    @POST("users/account")
    suspend fun getUserAccount(
        @Header("X-API-KEY") apiKey: String,
        @Body response: LoginRequest
    ): UserData

    suspend fun checkUserAccount(
        @Header("X-API-KEY") apiKey: String,
        @Body response: LoginRequest
    ): Boolean

    @POST("users/login")
    suspend fun login(
        @Header("AUTH-KEY") authKey: String,
        @Body request: LoginRequest
    ): Response<JsonObject>

    @POST("users/create_user")
    suspend fun createUser(
        @Header("AUTH-KEY") authKey: String,
        @Body user: SignUpData
    ): Response<JsonObject>

    @PUT("users/update")
    fun modifyUser(
        @Header("X-API-KEY") apiKey: String,
        @Body user: SignUpData
    ): Call<ResponseResult>

    @POST("messages/create_message")
    fun createMessage(
        @Header("X-API-KEY") apiKey: String,
        @Body messageData: MessageData
    ): Call<Void>

    @GET("messages/search/{field}/{filter}")
    fun getMessagesByFieldAndFilter(
        @Header("X-API-KEY") apiKey: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): Call<List<MessageData>>

    @GET("custom/{collection}")
    fun getCustomBsonDocuments(
        @Header("X-API-KEY") apiKey: String,
        @Path("collection") collection: String,
    ): Response<JsonObject>

    @POST("custom/create/{collection}")
    fun createCustomData(
        @Header("X-API-KEY") apiKey: String,
        @Path("collection") collection: String,
        @Body document: Map<String, Any>
    ): Call<Void>

    @GET("custom/search/{collection}/{field}/{filter}")
    fun getCustomBsonFilteredDocument(
        @Header("X-API-KEY") apiKey: String,
        @Path("collection") collection: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): Call<List<Map<String, Any>>>

    @DELETE("custom/delete/{collection}/{field}/{filter}")
    fun deleteCustomBsonDocument(
        @Header("X-API-KEY") apiKey: String,
        @Path("collection") collection: String,
        @Path("field") field: String,
        @Path("filter") filter: String
    ): Response<JsonObject>

}