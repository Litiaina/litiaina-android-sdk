package com.litiaina.android.sdk.api

import com.litiaina.android.sdk.data.LoginRequest
import com.litiaina.android.sdk.data.SignUpData
import com.litiaina.android.sdk.retrofit.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class Credential {
    companion object {
        suspend fun login(apiKey: String, email: String, password: String): Boolean {
            var validLogin = false
            coroutineScope {
                val response = async {
                    RetrofitInstance.userApi.login(
                        apiKey,
                        LoginRequest(email, password)
                    )
                }
                if (response.await().isSuccessful) {
                    if (response.await().body()?.get("response")?.asBoolean == true) {
                        validLogin = true
                    }
                }
            }
            return validLogin
        }
        suspend fun register(apiKey: String, name: String, email: String, password: String): Boolean {
            var validRegister = false
            coroutineScope {
                val response = async {
                    RetrofitInstance.userApi.createUser(
                        apiKey,
                        SignUpData(
                            name,
                            email,
                            password,
                            profilePicture = "",
                            accessLevel = "1",
                        )
                    )
                }
                if (response.await().isSuccessful) {
                    if (response.await().body()?.get("response")?.asBoolean == true) {
                        validRegister = true
                    }
                }
            }
            return validRegister
        }
    }
}