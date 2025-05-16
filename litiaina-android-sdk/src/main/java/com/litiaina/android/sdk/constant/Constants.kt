package com.litiaina.android.sdk.constant

internal object Constants {
    const val SHARE_PREFERENCES_LOCAL: String = "internal-litiaina-android-sdk-share-preferences-c377d548-12e0-4f12-a08e-dc589ddf37fa"
    const val SERVER_URL: String = "https://api.litiaina.com/"
    const val CLOUD_STORAGE_URL: String = "https://storage.litiaina.com/"
    const val SERVER_WEBSOCKET_URL: String = "wss://websocket.litiaina.com/"
    const val MAX_RECONNECT_ATTEMPTS = 5
    const val RECONNECT_DELAY = 5L
    const val PING_INTERVAL_MILLIS = 30_000L
    const val UPDATE_FILE_LIST_REAL_TIME = "retrieve-file-list-real-time"
    const val UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME = "retrieve-authenticated-user-data-real-time"
    const val AUTHORIZED_EMAIL = "authorized_email"
    const val AUTHORIZED_PASSWORD = "authorized_password"
    const val AUTHORIZED_TOKEN = "authorized_api_key"
    const val AUTHORIZED_UID = "authorized_uid"
}