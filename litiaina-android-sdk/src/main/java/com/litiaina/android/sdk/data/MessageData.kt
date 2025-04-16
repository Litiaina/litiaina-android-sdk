package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

internal data class MessageData(
    val channel: String,
    val content: String,
    val sender: String,
    val receiver: String,
    @SerializedName("date_time") val dateTime: String,
    val seen: Boolean = false
)
