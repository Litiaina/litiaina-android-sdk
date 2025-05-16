package com.litiaina.android.sdk.retrofit

import com.google.gson.JsonObject
import com.litiaina.android.sdk.data.AccountData
import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.ResponseResult
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.data.UpdateAuthData
import com.litiaina.android.sdk.data.UserData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

internal interface AuthApiService {

    @GET("ping")
    suspend fun pingApiServer(): Response<ResponseBody>

    @GET("auth")
    suspend fun getAllAuthAccounts(
        @Header("authorization") token: String,
    ): List<UserData>

    @POST("auth/account")
    suspend fun getAuthAccount(
        @Header("authorization") token: String,
        @Body response: LoginRequest
    ): AccountData

    suspend fun checkUserAccount(
        @Header("authorization") token: String,
        @Body response: LoginRequest
    ): Boolean

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<JsonObject>

    @POST("auth/create")
    suspend fun createAuth(
        @Header("authorization") token: String,
        @Body user: SignUpData
    ): Response<JsonObject>

    @PUT("auth/update")
    suspend fun modifyAuthData(
        @Header("authorization") token: String,
        @Body user: UpdateAuthData
    ): Response<ResponseResult>

    @PUT("auth/credentials/update")
    suspend fun modifyCredentialsUser(
        @Header("authorization") token: String,
        @Body user: SignUpData
    ): Response<ResponseResult>


}