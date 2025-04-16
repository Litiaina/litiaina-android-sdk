package com.litiaina.android.sdk.api

import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
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
                response.isSuccessful && response.body()?.get("response")?.asBoolean == true
            } catch (e: Exception) {
                false
            }
            withContext(Dispatchers.Main) {
                onResult(result)
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