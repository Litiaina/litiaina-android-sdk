package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.constant.Constants.UPDATE_USER_DATA_REAL_TIME
import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.data.UserData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Database {
    fun retrieveUserData(
        apiKey: String,
        email: String,
        password: String,
        onResult: (UserData?) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val userData: UserData? = try {
                RetrofitInstance.userApi.getUserAccount(apiKey, LoginRequest(email, password))
            } catch (e: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                onResult(userData)
            }
        }
    }

    fun retrieveUserDataRealtime(
        lifecycleOwner: LifecycleOwner,
        apiKey: String,
        uid: String,
        email: String,
        password: String,
        onResult: (UserData?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.messageLiveData.observe(lifecycleOwner) { message ->
            if (!(message.contains(UPDATE_USER_DATA_REAL_TIME))) {
                return@observe
            }

            if (message.contains("$uid: $UPDATE_USER_DATA_REAL_TIME")) {
                return@observe
            }

            CoroutineScope(Dispatchers.IO).launch {
                val userData: UserData? = try {
                    RetrofitInstance.userApi.getUserAccount(apiKey, LoginRequest(email, password))
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    onResult(userData)
                }
            }
        }
    }

    fun modifyUserData(
        apiKey: String,
        email: String? = null,
        password: String,
        name: String? = null,
        profilePicture: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val modified = try {
                val userData = RetrofitInstance.userApi.getUserAccount(
                    apiKey,
                    LoginRequest(email, password)
                )
                val response = RetrofitInstance.userApi.modifyUser(apiKey,
                    SignUpData(
                        name = name ?: userData.name,
                        email = email ?: userData.email,
                        profilePicture = profilePicture ?: userData.profilePicture,
                        password = "",
                        accessLevel = ""
                    )
                ).execute()
                if (response.isSuccessful) {
                    WebSocketManager.send(UPDATE_USER_DATA_REAL_TIME)
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