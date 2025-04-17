package com.litiaina.android.sdk.data

import com.google.gson.annotations.SerializedName

internal data class FileResponse(
    @SerializedName("files") val files: List<FileDetailData>
)

