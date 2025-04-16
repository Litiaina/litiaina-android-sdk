package com.litiaina.android.sdk.constant

internal class Constants {
    companion object {
        const val SHARE_PREFERENCES_LOCAL: String = "LITIAINA-dgl;iaejrtil;wjo12ij43oi23j4oi23nfsdfn"
        const val SERVER_URL: String = "https://api.litiaina.com/"
        const val CLOUD_STORAGE_URL: String = "https://storage.litiaina.com/"
        const val SERVER_WEBSOCKET_URL: String = "wss://websocket.litiaina.com/websocket"
        const val STOP_ACTION = "com.altear.platform.litiaina.STOP_SERVICE"
        const val AUTH_KEY = "1029384756"
        const val MAX_RECONNECT_ATTEMPTS = 5
        const val RECONNECT_DELAY = 5L
        const val FOREGROUND_CHANNEL_ID = "UPLOAD-FILE-SERVICE-CHANNEL"
        const val PING_INTERVAL_MILLIS = 30_000L
    }
}