package com.litiaina.android.sdk.api

import com.litiaina.android.sdk.constant.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object RetrofitInstance {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofitUserApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitStorageApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.CLOUD_STORAGE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApi: UserApiService by lazy {
        retrofitUserApi.create(UserApiService::class.java)
    }

    val storageApi: StorageApiService by lazy {
        retrofitStorageApi.create(StorageApiService::class.java)
    }

}