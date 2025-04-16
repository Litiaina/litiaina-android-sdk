package com.litiaina.android.sdk.api

import com.litiaina.android.sdk.websocket.WebSocketManager

object LitiainaInstance {
    private var initialized = false

    fun init(
        apiKey: String,
        uid: String,
        email: String
    ) {
        WebSocketManager.close()
        WebSocketManager.init(apiKey, uid, email)
        WebSocketManager.connect()
        WebSocketManager.refresh()
        initialized = true
    }

    fun close(){
        WebSocketManager.close()
    }

    fun ensureInitialized() {
        if (!initialized) {
            throw IllegalStateException("LitiainaInstance is not initialized. Please call LitiainaInstance.init() before using the SDK.")
        }
    }
}