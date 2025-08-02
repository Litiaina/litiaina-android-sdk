package com.litiaina.android.sdk.data

data class NotificationPushRequest(
    val token: String,
    val title: String,
    val body: String
)

data class MultiplePushRequest(
    val tokens: List<String>,
    val title: String,
    val body: String
)