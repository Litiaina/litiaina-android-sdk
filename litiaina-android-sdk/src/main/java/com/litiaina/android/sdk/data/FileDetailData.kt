package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

data class FileDetailData(
    val name: String,
    val created: Long,
    @SerializedName("file_type") val fileType: String,
    val size: Long
)
