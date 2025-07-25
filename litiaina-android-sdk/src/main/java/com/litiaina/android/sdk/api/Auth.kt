package com.litiaina.android.sdk.api

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
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
import com.litiaina.android.sdk.interfaces.LoginResult
import com.litiaina.android.sdk.interfaces.ModifyResult
import com.litiaina.android.sdk.interfaces.TwoFAResult
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.IOException

object Auth {
    fun login(
        email: String,
        password: String,
        otp: String? = null,
        onResult: (LoginResult) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val loginRequest = LoginRequest(email, password, otp)
                val response = RetrofitInstance.authApi.login(loginRequest)
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.get("token")?.asString
                    val isValid = body?.get("response")?.asBoolean ?: false
                    val account = RetrofitInstance.authApi.getAuthAccount("Bearer $token", loginRequest)
                    if (account.uid.isNotEmpty()) {
                        getSharedPreferences()?.edit()?.apply {
                            putString(AUTHORIZED_EMAIL, email)
                            putString(AUTHORIZED_PASSWORD, password)
                            putString(AUTHORIZED_TOKEN, token)
                            putString(AUTHORIZED_UID, account.uid)
                            apply()
                        }
                        WebSocketManager.run {
                            close()
                            init(token.orEmpty(), getUID(), account.uid)
                            connect()
                            refresh()
                        }
                        LoginResult.Success(isValid)
                    } else
                        LoginResult.Failure("Failed to retrieve account data")
                } else
                    LoginResult.Success(false)
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    is JsonParseException -> "Response parsing error"
                    else -> "Unexpected error: ${e.message}"
                }
                LoginResult.Failure(message)
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun check2FAEnabledAccount(email: String, password: String, onResult: (TwoFAResult) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.authApi.verifyAccount2FA(
                    LoginRequest(email = email, password = password)
                )
                if (response.isSuccessful) {
                    val enabled = response.body()?.get("enabled")?.asBoolean ?: false
                    TwoFAResult.Success(enabled)
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        val json = JsonParser.parseString(errorBody).asJsonObject
                        json["error"]?.asString ?: "Unknown error"
                    } catch (e: Exception) {
                        "Failed to parse error response"
                    }
                    TwoFAResult.Failure(errorMessage)
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    is JsonParseException -> "Response parsing error"
                    else -> "Unexpected error: ${e.message}"
                }
                TwoFAResult.Failure(message)
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
        WebSocketManager.authMessageLiveData.observe(lifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                val accountData: AccountData? = try {
                    RetrofitInstance.authApi.getAuthAccount(
                        "Bearer ${getSharedPreferences()!!.getString(AUTHORIZED_TOKEN, "")}",
                        LoginRequest(
                            getSharedPreferences()!!.getString(AUTHORIZED_EMAIL, "").orEmpty(),
                            getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD, "").orEmpty()
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
        onResult: (ModifyResult) -> Unit
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
                        ModifyResult.Success(true)
                    } else ModifyResult.Failure("Failed to modify")
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        val json = JsonParser.parseString(errorBody).asJsonObject
                        json["error"]?.asString ?: "Unknown error"
                    } catch (e: Exception) {
                        "Failed to parse error response"
                    }
                    ModifyResult.Failure(errorMessage)
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    is JsonParseException -> "Response parsing error"
                    else -> "Unexpected error: ${e.message}"
                }
                ModifyResult.Failure(message)
            }

            withContext(Dispatchers.Main) {
                onResult(modified)
            }
        }
    }
}