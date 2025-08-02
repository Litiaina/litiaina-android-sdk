package com.litiaina.android.sdk.retrofit

import com.litiaina.android.sdk.data.FileResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

internal interface StorageApiService {

    @GET("ping")
    suspend fun pingStorageServer(): Response<ResponseBody>

    @GET("dir/{path}")
    suspend fun getFilesList(
        @Header("authorization") apiKey: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null
    ): FileResponse

    @Multipart
    @POST("dir/file/upload/{path}")
    suspend fun uploadFile(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("dir/file/upload/v2/{path}")
    suspend fun uploadFileV2(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null,
        @Query("file_id") fileId: String,
        @Query("chunk_index") chunkIndex: Int,
        @Query("total_chunks") totalChunks: Int,
        @Query("original_filename") originalFilename: String?,
        @Query("mime_type") mimeType: String?,
        @Query("checksum") checksum: String?,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @POST("dir/create/{path}")
    fun finalizeDirectory(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null,
    ): Call<ResponseBody>

    @PUT("dir/file/rename/{path}")
    fun renameFile(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null,
        @Query("file_to_modify") fileToModify: String,
        @Query("new_file_name") newFileName: String,
    ): Call<ResponseBody>

    @DELETE("dir/file/delete/{path}")
    fun deleteFile(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String? = null,
        @Query("file_to_delete") fileToDelete: String,
    ): Call<ResponseBody>

    @GET("dir/file/download/{path}")
    @Streaming
    fun downloadFile(
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String
    ): Call<ResponseBody>

    @POST("dir/create/{path}")
    fun createDirectory(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String,
    ): Call<ResponseBody>

    @DELETE("dir/delete/{path}")
    fun deleteDirectory(
        @Header("authorization") token: String,
        @Header("uid") uid: String,
        @Path(value = "path", encoded = true) path: String,
    ): Call<ResponseBody>

}