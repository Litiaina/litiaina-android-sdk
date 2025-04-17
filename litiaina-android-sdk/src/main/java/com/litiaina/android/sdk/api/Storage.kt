package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
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

object Storage {
    fun updateFileList() {
        WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
    }
    fun retrieveFileList(
        apiKey: String,
        email: String,
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            var files: List<FileDetailData>? = null
            RetrofitInstance.storageApi.getFilesList(apiKey , serializeEmailPath(email)).enqueue(object :
                Callback<FileResponse> {
                override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.files?.let { filesList ->
                            files = filesList
                        }
                    } else {
                        Log.e("MainActivity", "Request failed with code: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                    Log.e("RetrieveFileList", "Request failed", t)
                }
            })

            withContext(Dispatchers.Main) {
                onResult(files)
            }
        }
    }

    fun retrieveFileListRealtime(
        lifecycleOwner: LifecycleOwner,
        apiKey: String,
        uid: String,
        email: String,
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.messageLiveData.observe(lifecycleOwner) { message ->
            if (!(message.contains(UPDATE_FILE_LIST_REAL_TIME))) {
                return@observe
            }

            if (message.contains("$uid: $UPDATE_FILE_LIST_REAL_TIME")) {
                return@observe
            }

            CoroutineScope(Dispatchers.IO).launch {
                var files: List<FileDetailData>? = null
                RetrofitInstance.storageApi.getFilesList(apiKey , serializeEmailPath(email)).enqueue(object :
                    Callback<FileResponse> {
                    override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.files?.let { filesList ->
                                files = filesList
                            }
                        } else {
                            Log.e("MainActivity", "Request failed with code: ${response.code()}")
                        }
                    }
                    override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                        Log.e("RetrieveFileList", "Request failed", t)
                    }
                })

                withContext(Dispatchers.Main) {
                    onResult(files)
                }
            }
        }
    }

    fun deleteFile(
        apiKey: String,
        email: String,
        fileName: String,
        onResult: (Boolean) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val operationSuccess = try {
                val response = RetrofitInstance.storageApi.deleteFile(apiKey, serializeEmailFilePath(email,fileName)).execute()
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