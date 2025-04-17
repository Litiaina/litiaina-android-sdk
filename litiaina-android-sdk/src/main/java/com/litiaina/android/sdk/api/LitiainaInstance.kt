package com.litiaina.android.sdk.api

import android.content.Context
import android.content.SharedPreferences
import com.litiaina.android.sdk.constant.Constants.SHARE_PREFERENCES_LOCAL
import com.litiaina.android.sdk.websocket.WebSocketManager
import java.util.UUID

object LitiainaInstance {
    private lateinit var uid: UUID
    private var internalSharedPreferences: SharedPreferences? = null
    private var initialized = false

    fun init(appContext: Context) {
        uid = UUID.randomUUID()
        internalSharedPreferences = appContext.applicationContext.getSharedPreferences(SHARE_PREFERENCES_LOCAL, Context.MODE_PRIVATE)
        initialized = true
    }

    internal fun getSharedPreferences(): SharedPreferences? {
        ensureInitialized()
        return internalSharedPreferences
    }

    internal fun getUID(): String {
        ensureInitialized()
        return uid.toString()
    }

    fun destroy(){
        with(internalSharedPreferences!!.edit()) {
            remove("email")
            remove("password")
            remove("api_key")
            apply()
        }
        internalSharedPreferences = null
        WebSocketManager.close()
    }

    fun ensureInitialized() {
        if (!initialized) {
            throw IllegalStateException("LitiainaInstance is not initialized. Please call LitiainaInstance.init() before using the SDK.")
        }
    }
}