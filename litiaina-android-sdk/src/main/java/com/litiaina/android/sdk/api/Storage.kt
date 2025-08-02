package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_TOKEN
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_UID
import com.litiaina.android.sdk.constant.Constants.UPDATE_FILE_LIST_REAL_TIME
import com.litiaina.android.sdk.data.FileDetailData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

object Storage {
    @Volatile
    private var isFetching = false

    fun updateFileList() {
        WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
    }

    private suspend fun getFilesListAsync(
        apiKey: String,
        uid: String,
        path: String? = null
    ): List<FileDetailData>? {
        return try {
            return RetrofitInstance.storageApi.getFilesList(
                apiKey = apiKey,
                uid = uid,
                path = path ?: ""
            ).files.ifEmpty {
                listOf()
            }
        } catch (e: Exception) {
            Log.e("getFilesListAsync", "Error fetching file list", e)
            listOf()
        }
    }

    fun retrieveFileList(
        path: String? = null,
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val files = try {
                getFilesListAsync(
                    apiKey = "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    uid = getSharedPreferences()?.getString(AUTHORIZED_UID, "").orEmpty(),
                    path = path ?: "",
                )
            } catch (e: Exception) {
                Log.e("retrieveFileList", "Error retrieving files", e)
                null
            }

            withContext(Dispatchers.Main) {
                onResult(files)
            }
        }
    }

    fun retrieveFileListRealtime(
        path: String? = null,
        lifecycleOwner: LifecycleOwner,
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.fileListMessageLiveData.observe(lifecycleOwner) {
            if (isFetching) return@observe

            isFetching = true
            CoroutineScope(Dispatchers.IO).launch {
                val files = try {
                    getFilesListAsync(
                        apiKey = "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN, "").toString()}",
                        uid = getSharedPreferences()?.getString(AUTHORIZED_UID, "").orEmpty(),
                        path = path ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("retrieveFileList", "Exception during file fetch", e)
                    null
                }

                withContext(Dispatchers.Main) {
                    isFetching = false
                    onResult(files)
                }
            }
        }
    }

    fun deleteFile(
        path: String? = null,
        fileName: String,
        onResult: (Boolean) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val operationSuccess = try {
                val response = RetrofitInstance.storageApi.deleteFile(
                    token = "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    uid = getSharedPreferences()!!.getString(AUTHORIZED_UID, "").toString(),
                    path = path ?: "",
                    fileToDelete = fileName
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

    fun renameFile(
        path: String? = null,
        oldFileName: String,
        newFileName: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val modified = try {
                val response = RetrofitInstance.storageApi.renameFile(
                    token = "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    uid = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                    path = path ?: "",
                    fileToModify = oldFileName,
                    newFileName = newFileName,
                ).execute()
                if (response.isSuccessful) {
                    WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
                    true
                } else
                    false
            } catch (e: Exception) {
                Log.e("ModifyUserData", "Exception occurred: ${e.message}", e)
                false
            }

            withContext(Dispatchers.Main) {
                onResult(modified)
            }
        }
    }

    fun upload(
        path: String? = null,
        multipartBody: MultipartBody.Part,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val uploaded = try {
                val response = RetrofitInstance.storageApi.uploadFile(
                    token = "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    uid = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                    path = path,
                    file = multipartBody
                )
                if (response.isSuccessful) {
                    WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
                    true
                } else false
            } catch (e: Exception) {
                Log.e("Upload", "Exception occurred: ${e.message}", e)
                false
            }

            withContext(Dispatchers.Main) {
                onResult(uploaded)
            }
        }
    }
}