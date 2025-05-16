package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.api.LitiainaInstance.getUID
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_TOKEN
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_PASSWORD
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_UID
import com.litiaina.android.sdk.constant.Constants.UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME
import com.litiaina.android.sdk.data.AccountData
import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.data.UpdateAuthData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Auth {
    fun login(
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.authApi.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body()?.get("response")?.asBoolean == true) {
                    val account = RetrofitInstance.authApi.getAuthAccount("Bearer ${response.body()?.get("token")?.asString}", LoginRequest(email, password))
                    if (account.uid.isNotEmpty()) {
                        getSharedPreferences()?.let { internalMemory ->
                            with(internalMemory.edit()) {
                                putString(AUTHORIZED_EMAIL, email)
                                putString(AUTHORIZED_PASSWORD, password)
                                putString(AUTHORIZED_TOKEN, response.body()?.get("token")?.asString)
                                putString(AUTHORIZED_UID, account.uid)
                                apply()
                            }
                            WebSocketManager.close()
                            WebSocketManager.init(
                                internalMemory.getString(AUTHORIZED_TOKEN,"").toString(),
                                getUID(),
                                internalMemory.getString(AUTHORIZED_UID,"").toString())
                            WebSocketManager.connect()
                            WebSocketManager.refresh()
                        }
                        true
                    } else false
                } else
                    false
            } catch (e: Exception) {
                Log.e("login", "Exception occurred: ${e.message}", e)
                false
            }
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun logout() {
        getSharedPreferences()?.let { internalMemory ->
            with(internalMemory.edit()) {
                remove(AUTHORIZED_EMAIL)
                remove(AUTHORIZED_PASSWORD)
                remove(AUTHORIZED_TOKEN)
                remove(AUTHORIZED_UID)
                apply()
            }
        }
    }

    fun register(
        authKey: String,
        name: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val data = SignUpData(name, email, password, "")
                val response = RetrofitInstance.authApi.createAuth("Bearer $authKey", data)
                response.isSuccessful && response.body()?.get("response")?.asBoolean == true
            } catch (e: Exception) {
                Log.e("register", "Exception occurred: ${e.message}", e)
                false
            }
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun updateAuthenticatedUserData() {
        WebSocketManager.send(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME)
    }

    fun retrieveAuthenticatedUserData(onResult: (AccountData?) -> Unit) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val accountData: AccountData? = try {
                RetrofitInstance.authApi.getAuthAccount(
                    "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    LoginRequest(
                        getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString()
                    )
                )
            } catch (e: Exception) {
                Log.e("retrieveAuthenticatedUserData", "Exception occurred: ${e.message}", e)
                null
            }
            withContext(Dispatchers.Main) {
                onResult(accountData)
            }
        }
    }

    fun retrieveAuthenticatedUserDataRealtime(
        lifecycleOwner: LifecycleOwner,
        onResult: (AccountData?) -> Unit
    ) {
        ensureInitialized()
        WebSocketManager.messageLiveData.observe(lifecycleOwner) { message ->
            if (!(message.contains(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME))) {
                return@observe
            }

            if (message.contains("${getUID()}: $UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME")) {
                return@observe
            }

            CoroutineScope(Dispatchers.IO).launch {
                val accountData: AccountData? = try {
                    RetrofitInstance.authApi.getAuthAccount(
                        "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                        LoginRequest(
                            getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                            getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString()
                        )
                    )
                } catch (e: Exception) {
                    Log.e("retrieveAuthenticatedUserDataRealtime", "Exception occurred: ${e.message}", e)
                    null
                }
                withContext(Dispatchers.Main) {
                    onResult(accountData)
                }
            }
        }
    }

    fun modifyAuthenticatedUserData(
        newName: String? = null,
        newProfilePicture: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val modified = try {
                val response = RetrofitInstance.authApi.modifyAuthData(
                    "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN,"").toString()}",
                    UpdateAuthData(
                        currentEmail = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        currentPassword = getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString(),
                        newName = newName,
                        profilePicture = newProfilePicture
                    )
                )
                if (response.isSuccessful) {
                    if (response.body()?.response == true) {
                        WebSocketManager.send(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME)
                        true
                    } else false
                } else false
            } catch (e: Exception) {
                Log.e("modifyAuthenticatedUserData", "Exception occurred: ${e.message}", e)
                false
            }

            withContext(Dispatchers.Main) {
                onResult(modified)
            }
        }
    }
}