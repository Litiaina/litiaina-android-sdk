package com.litiaina.android.sdk.api

import com.google.gson.JsonParser
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_PASSWORD
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_TOKEN
import com.litiaina.android.sdk.data.DeviceTokenRegistryRequest
import com.litiaina.android.sdk.data.DeviceTokenRemovalRequest
import com.litiaina.android.sdk.data.MultiplePushRequest
import com.litiaina.android.sdk.data.NotificationPushRequest
import com.litiaina.android.sdk.data.NotificationTokenRequest
import com.litiaina.android.sdk.interfaces.NotificationResult
import com.litiaina.android.sdk.interfaces.PushNotificationResult
import com.litiaina.android.sdk.interfaces.RegisterDeviceTokenResult
import com.litiaina.android.sdk.interfaces.RemoveDeviceTokenResult
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.platform.Platform

object Notification {
    fun sendSinglePush(
        request: NotificationPushRequest,
        onResult: (PushNotificationResult) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val token = getSharedPreferences()?.getString(AUTHORIZED_TOKEN, "").orEmpty()
                val response = RetrofitInstance.notificationApi.sendSingleMessage("Bearer $token", request)

                if (response.isSuccessful) {
                    val successMsg = response.body()?.get("success")?.asString ?: "Push sent"
                    PushNotificationResult.Success(successMsg)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    PushNotificationResult.Failure("Failed: $errorMsg")
                }
            } catch (e: Exception) {
                PushNotificationResult.Failure("Exception: ${e.localizedMessage}")
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun sendMultiplePush(
        request: MultiplePushRequest,
        onResult: (PushNotificationResult) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val token = getSharedPreferences()?.getString(AUTHORIZED_TOKEN, "").orEmpty()
                val response = RetrofitInstance.notificationApi.sendMultipleMessage("Bearer $token", request)

                if (response.isSuccessful) {
                    val successMsg = response.body()?.get("success")?.asString ?: "Multiple push sent"
                    PushNotificationResult.Success(successMsg)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    PushNotificationResult.Failure("Failed: $errorMsg")
                }
            } catch (e: Exception) {
                PushNotificationResult.Failure("Exception: ${e.localizedMessage}")
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun registerDeviceTokenToAccount(
        otp: String?,
        token: String,
        platform: String,
        deviceInfo: String? = null,
        onResult: (RegisterDeviceTokenResult) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.notificationApi.addDeviceTokenToAccount(
                    token = "Bearer ${getSharedPreferences()?.getString(AUTHORIZED_TOKEN, "").toString()}",
                    request = DeviceTokenRegistryRequest(
                        email = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        password = getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString(),
                        otp = otp,
                        token = token,
                        platform = platform,
                        deviceInfo = deviceInfo
                    )
                )
                if (response.isSuccessful) {
                    val successMsg = response.body()?.get("success")?.asString ?: "Token registry failed"
                    RegisterDeviceTokenResult.Success(successMsg)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    RegisterDeviceTokenResult.Failure("Failed: $errorMsg")
                }
            } catch (e: Exception) {
                RegisterDeviceTokenResult.Failure("Exception: ${e.localizedMessage}")
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun removeDeviceTokenFromAccount(
        otp: String? = null,
        platform: String? = null,
        deviceInfo: String? = null,
        onResult: (RemoveDeviceTokenResult) -> Unit
    ) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.notificationApi.removeDeviceTokenToAccount(
                    token = "Bearer ${getSharedPreferences()?.getString(AUTHORIZED_TOKEN, "").toString()}",
                    request = DeviceTokenRemovalRequest(
                        email = getSharedPreferences()!!.getString(AUTHORIZED_EMAIL,"").toString(),
                        password = getSharedPreferences()!!.getString(AUTHORIZED_PASSWORD,"").toString(),
                        otp = otp,
                        platform = platform,
                        deviceInfo = deviceInfo
                    )
                )
                if (response.isSuccessful) {
                    val successMsg = response.body()?.get("success")?.asString ?: "Token removal failed"
                    RemoveDeviceTokenResult.Success(successMsg)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    RemoveDeviceTokenResult.Failure("Failed: $errorMsg")
                }
            } catch (e: Exception) {
                RemoveDeviceTokenResult.Failure("Exception: ${e.localizedMessage}")
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun fetchNotificationToken(uid: String, onResult: (NotificationResult) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.notificationApi.getNotificationToken(
                    token = "Bearer ${getSharedPreferences()?.getString(AUTHORIZED_TOKEN, "").toString()}",
                    request = NotificationTokenRequest(uid)
                )
                if (response.isSuccessful) {
                    val tokens = response.body()?.result.orEmpty()
                    if (tokens.isNotEmpty()) {
                        NotificationResult.Success(tokens)
                    } else {
                        NotificationResult.Failure("No tokens found for this UID.")
                    }
                } else {
                    NotificationResult.Failure("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                NotificationResult.Failure("Exception: ${e.localizedMessage}")
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    internal fun parseErrorMessage(json: String?): String {
        return try {
            val element = JsonParser.parseString(json).asJsonObject
            element["error"]?.asString ?: "Unknown error"
        } catch (e: Exception) {
            "Malformed error response"
        }
    }
}