package com.litiaina.android.sdk.api

import com.google.gson.JsonParser
import com.litiaina.android.sdk.api.LitiainaInstance.ensureInitialized
import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_TOKEN
import com.litiaina.android.sdk.data.MultiplePushRequest
import com.litiaina.android.sdk.data.NotificationPushRequest
import com.litiaina.android.sdk.interfaces.PushNotificationResult
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private fun parseErrorMessage(json: String?): String {
        return try {
            val element = JsonParser.parseString(json).asJsonObject
            element["error"]?.asString ?: "Unknown error"
        } catch (e: Exception) {
            "Malformed error response"
        }
    }
}