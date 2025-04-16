package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.constant.Constants.UPDATE_FILE_LIST_REAL_TIME
import com.litiaina.android.sdk.data.FileListData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailFilePath
import com.litiaina.android.sdk.util.Format.Companion.serializeEmailPath
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Storage {
    fun retrieveFileList(
        apiKey: String,
        email: String,
        onResult: (FileListData?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val fileList: FileListData? = try {
                RetrofitInstance.storageApi.getFilesList(apiKey, serializeEmailPath(email))
            } catch (e: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                onResult(fileList)
            }
        }
    }

    fun retrieveFileListRealtime(
        lifecycleOwner: LifecycleOwner,
        apiKey: String,
        uid: String,
        email: String,
        onResult: (FileListData?) -> Unit
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
                val fileList: FileListData? = try {
                    RetrofitInstance.storageApi.getFilesList(apiKey, serializeEmailPath(email))
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    onResult(fileList)
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