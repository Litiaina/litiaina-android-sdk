package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

data class FileListData(
    @SerializedName("files") val files: List<FileDetailData>
)

