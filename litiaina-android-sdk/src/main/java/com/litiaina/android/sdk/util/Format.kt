package com.litiaina.android.sdk.util

import com.litiaina.android.sdk.constant.Constants

internal class Format {
    companion object {
        fun getFileUrl(directory: String, fileName: String): String {
            return "${Constants.CLOUD_STORAGE_URL}files/stream_file/$directory/$fileName"
        }

        fun getDownloadUrl(directory: String, fileName: String): String {
            return "${Constants.CLOUD_STORAGE_URL}files/view_file/$directory/$fileName"
        }

        fun getDownloadFileUrl(directory: String, fileName: String): String {
            return "${Constants.CLOUD_STORAGE_URL}files/download/$directory/$fileName"
        }

        fun formatChannel(sender: String, receiver: String): String {
            val input = "$sender~$receiver"
            val splitInput = input.split("~")
            val sort = splitInput.sorted()
            return sort.joinToString("~")
        }
    }
}