package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.api.LitiainaInstance.getUID
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_API_KEY
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.UPDATE_FILE_LIST_REAL_TIME
import com.litiaina.android.sdk.data.FileDetailData
import com.litiaina.android.sdk.data.FileResponse
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailFilePath
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailPath
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Storage {
    fun updateFileList() {
        WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
    }

    private suspend fun getFilesListAsync(apiKey: String, email: String): List<FileDetailData>? = suspendCoroutine { continuation ->
        RetrofitInstance.storageApi.getFilesList(apiKey = apiKey, path = serializeEmailPath(email))
            .enqueue(object : Callback<FileResponse> {
                override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body()?.files)
                    } else {
                        Log.e("MainActivity", "Request failed with code: ${response.code()}")
                        continuation.resume(null)
                    }
                }

                override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                    Log.e("RetrieveFileList", "Request failed", t)
                    continuation.resume(null)
                }
            })
    }

    fun retrieveFileList(
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).launch {
                val files: List<FileDetailData>? = try {
                    getFilesListAsync(
                        getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString()
                    )
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    onResult(files)
                }
            }
        }
    }

    fun retrieveFileListRealtime(
        lifecycleOwner: LifecycleOwner,
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.messageLiveData.observe(lifecycleOwner) { message ->
            if (!(message.contains(UPDATE_FILE_LIST_REAL_TIME))) {
                return@observe
            }

            if (message.contains("${getUID()}: $UPDATE_FILE_LIST_REAL_TIME")) {
                return@observe
            }

            CoroutineScope(Dispatchers.IO).launch {
                val files: List<FileDetailData>? = try {
                    getFilesListAsync(
                        getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString()
                    )
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    onResult(files)
                }
            }
        }
    }

    fun deleteFile(
        fileName: String,
        onResult: (Boolean) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val operationSuccess = try {
                val response = RetrofitInstance.storageApi.deleteFile(
                    getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                    serializeEmailFilePath(
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        fileName
                    )
                ).execute()
                if (response.isSuccessful) {
                    WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("DeleteFile", "Exception during file delete: ${e.message}", e)
                false
            }

            withContext(Dispatchers.Main) {
                onResult(operationSuccess)
            }
        }
    }
}