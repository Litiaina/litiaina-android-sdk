package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.api.LitiainaInstance.getUID
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_API_KEY
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_PASSWORD
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
    fun updateUserData() {
        WebSocketManager.send(UPDATE_USER_DATA_REAL_TIME)
    }
    fun retrieveUserData(onResult: (UserData?) -> Unit) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val userData: UserData? = try {
                RetrofitInstance.userApi.getUserAccount(
                    getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                    LoginRequest(
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString()
                    )
                )
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
        onResult: (UserData?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.messageLiveData.observe(lifecycleOwner) { message ->
            if (!(message.contains(UPDATE_USER_DATA_REAL_TIME))) {
                return@observe
            }

            if (message.contains("${getUID()}: $UPDATE_USER_DATA_REAL_TIME")) {
                return@observe
            }

            CoroutineScope(Dispatchers.IO).launch {
                val userData: UserData? = try {
                    RetrofitInstance.userApi.getUserAccount(
                        getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                        LoginRequest(
                            getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                            getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString()
                        )
                    )
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
        newName: String? = null,
        newProfilePicture: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val modified = try {
                val userData = RetrofitInstance.userApi.getUserAccount(
                    getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                    LoginRequest(
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString()
                    )
                )
                val response = RetrofitInstance.userApi.modifyUser(
                    getSharedPreferences()!!.getString(AUTHORIZED_API_KEY,"").toString(),
                    SignUpData(
                        name = newName ?: userData.name,
                        email = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        profilePicture = newProfilePicture ?: userData.profilePicture,
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