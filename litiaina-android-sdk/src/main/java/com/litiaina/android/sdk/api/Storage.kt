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
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailFilePath
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailPath
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Storage {

    @Volatile
    private var isFetching = false

    fun updateFileList() {
        WebSocketManager.send(UPDATE_FILE_LIST_REAL_TIME)
    }

    private suspend fun getFilesListAsync(apiKey: String, email: String): List<FileDetailData>? {
        return try {
            val response = RetrofitInstance.storageApi.getFilesList(apiKey, serializeEmailPath(email))
            response.files
        } catch (e: Exception) {
            Log.e("getFilesListAsync", "Error fetching file list", e)
            null
        }
    }

    fun retrieveFileList(
        onResult: (List<FileDetailData>?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val files = try {
                getFilesListAsync(
                    getSharedPreferences()?.getString(AUTHORIZED_API_KEY, "").orEmpty(),
                    getSharedPreferences()?.getString(AUTHORIZED_EMAIL, "").orEmpty()
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

            if (isFetching) return@observe

            isFetching = true
            CoroutineScope(Dispatchers.IO).launch {
                val files = try {
                    getFilesListAsync(
                        getSharedPreferences()?.getString(AUTHORIZED_API_KEY, "").orEmpty(),
                        getSharedPreferences()?.getString(AUTHORIZED_EMAIL, "").orEmpty()
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

    fun renameFile(
        oldFileName: String,
        newFileName: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val modified = try {
                val response = RetrofitInstance.storageApi.renameFile(
                    getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                    newFileName = newFileName,
                    serializeEmailFilePath(getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(), oldFileName)
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
}