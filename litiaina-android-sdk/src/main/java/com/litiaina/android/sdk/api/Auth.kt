package com.litiaina.android.sdk.api

import com.litiaina.android.sdk.api.LitiainaInstance.getSharedPreferences
import com.litiaina.android.sdk.api.LitiainaInstance.getUID
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_API_KEY
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_EMAIL
import com.litiaina.android.sdk.constant.Constants.AUTHORIZED_PASSWORD
import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import com.litiaina.android.sdk.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Auth {
    fun login(
        authKey: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                val response = RetrofitInstance.userApi.login(authKey, LoginRequest(email, password))
                if (response.isSuccessful && response.body()?.get("response")?.asBoolean == true) {
                    getSharedPreferences()?.let { internalMemory ->
                        with(internalMemory.edit()) {
                            putString(AUTHORIZED_EMAIL, email)
                            putString(AUTHORIZED_PASSWORD, password)
                            putString(AUTHORIZED_API_KEY, response.body()?.get("api_key")?.asString)
                            apply()
                        }
                        WebSocketManager.close()
                        WebSocketManager.init(
                            internalMemory.getString(AUTHORIZED_API_KEY,"").toString(),
                            getUID(),
                            internalMemory.getString(AUTHORIZED_EMAIL,"").toString())
                        WebSocketManager.connect()
                        WebSocketManager.refresh()
                    }
                    true
                } else
                    false
            } catch (e: Exception) {
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
                remove(AUTHORIZED_API_KEY)
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
                val data = SignUpData(name, email, password, "", "1")
                val response = RetrofitInstance.userApi.createUser(authKey, data)
                response.isSuccessful && response.body()?.get("response")?.asBoolean == true
            } catch (e: Exception) {
                false
            }
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}