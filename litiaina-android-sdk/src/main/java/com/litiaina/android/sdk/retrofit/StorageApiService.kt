package com.litiaina.android.sdk.retrofit

import com.litiaina.android.sdk.data.FileListData
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

internal interface StorageApiService {

    @GET("files/{path}")
    fun getFilesList(
        @Header("X-API-KEY") apiKey: String,
        @Path(value = "path", encoded = true) path: String
    ): FileListData

    @Multipart
    @POST("files/upload/{path}")
    suspend fun uploadFile(
        @Header("X-API-KEY") apiKey: String,
        @Path("path") path: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("files/upload/v2/{path}")
    suspend fun uploadFileInChunks(
        @Header("X-API-KEY") apiKey: String,
        @Path("path") path: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @POST("files/rename_file/{new_file_name}/{path}")
    fun renameFile(
        @Header("X-API-KEY") apiKey: String,
        @Path("new_file_name") newFileName: String,
        @Path(value = "path", encoded = true) path: String
    ): Call<ResponseBody>

    @GET("files/download/{path}")
    @Streaming
    fun downloadFile(
        @Header("X-API-KEY") apiKey: String,
        @Path(value = "path", encoded = true) path: String
    ): Call<ResponseBody>

    @POST("files/create_directory/{path}")
    fun createDirectory(
        @Header("X-API-KEY") apiKey: String,
        @Path(value = "path", encoded = true) path: String
    ): Call<ResponseBody>

    @DELETE("files/delete_file/{path}")
    fun deleteFile(
        @Header("X-API-KEY") apiKey: String,
        @Path(value = "path", encoded = true) path: String
    ): Call<ResponseBody>

}