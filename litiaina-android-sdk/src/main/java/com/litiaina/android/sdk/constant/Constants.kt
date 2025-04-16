package com.litiaina.android.sdk.constant

internal object Constants {
    const val SHARE_PREFERENCES_LOCAL: String = "LITIAINA-dgl;iaejrtil;wjo12ij43oi23j4oi23nfsdfn"
    const val SERVER_URL: String = "https://api.litiaina.com/"
    const val CLOUD_STORAGE_URL: String = "https://storage.litiaina.com/"
    const val SERVER_WEBSOCKET_URL: String = "wss://websocket.litiaina.com/websocket"
    const val STOP_ACTION = "com.altear.platform.litiaina.STOP_SERVICE"
    const val MAX_RECONNECT_ATTEMPTS = 5
    const val RECONNECT_DELAY = 5L
    const val FOREGROUND_CHANNEL_ID = "UPLOAD-FILE-SERVICE-CHANNEL"
    const val PING_INTERVAL_MILLIS = 30_000L
    const val UPDATE_FILE_LIST_REAL_TIME = "retrieve-file-list-real-time"
    const val UPDATE_USER_DATA_REAL_TIME = "retrieve-user-data-real-time"
}