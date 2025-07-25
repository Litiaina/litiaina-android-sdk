package com.litiaina.android.sdk.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.litiaina.android.sdk.api.LitiainaInstance
import com.litiaina.android.sdk.constant.Constants.MAX_RECONNECT_ATTEMPTS
import com.litiaina.android.sdk.constant.Constants.PING_INTERVAL_MILLIS
import com.litiaina.android.sdk.constant.Constants.RECONNECT_DELAY
import com.litiaina.android.sdk.constant.Constants.SERVER_WEBSOCKET_URL
import com.litiaina.android.sdk.constant.Constants.UPDATE_FILE_LIST_REAL_TIME
import com.litiaina.android.sdk.constant.Constants.UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

internal object WebSocketManager {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder().build()

    private val _authMessageLiveData = MutableLiveData<Unit>()
    val authMessageLiveData: LiveData<Unit> get() = _authMessageLiveData

    private val _fileListMessageLiveData = MutableLiveData<Unit>()
    val fileListMessageLiveData: LiveData<Unit> get() = _fileListMessageLiveData

    private val reconnectAttempts = AtomicInteger(0)
    private val messageQueue = mutableListOf<String>()
    private var connected = false

    private var uid: String? = null
    private var channel: String? = null
    private var apiKey: String? = null
    private var reconnectionValid = true
    private var isManualClose = false

    private val pingHandler = Handler(Looper.getMainLooper())
    private var pingRunnable: Runnable? = null

    fun init(apiKey: String, uid: String, channel: String) {
        this.apiKey = apiKey
        this.uid = uid
        this.channel = channel
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Initialized with uid=$uid, channel=$channel")
    }

    fun connect() {
        if (connected || reconnectAttempts.get() > MAX_RECONNECT_ATTEMPTS) return

        connected = false
        reconnectionValid = true
        isManualClose = false
        messageQueue.clear()

        webSocket?.close(1000, "Reconnecting")

        apiKey?.let { key ->
            val url = "$SERVER_WEBSOCKET_URL?uid=$uid&channel=$channel"
            val request = Request.Builder()
                .url(url)
                .addHeader("authorization", "Bearer $key")
                .build()

            if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Attempting connection to: $url")

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "WebSocket connected")
                    reconnectAttempts.set(0)
                    connected = true
                    webSocket.send("connected in channel: $channel")
                    flushMessageQueue()
                    startPing()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Message received: $text")
                    if (text == "ping") return
                    channel?.let {
                        if (text.contains(it)) return
                    }
                    dispatchMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "WebSocket closing: $code, $reason")
                    connected = false
                    reconnectionValid = false
                    webSocket.close(1000, null)
                    stopPing()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (LitiainaInstance.enabledDebug) Log.e("LitiainaAndroidSDK", "WebSocket failure: ${t.message}", t)
                    connected = false
                    stopPing()

                    if (response?.code == 401) {
                        if (LitiainaInstance.enabledDebug) Log.e("LitiainaAndroidSDK", "Unauthorized. No further reconnection attempts.")
                        reconnectionValid = false
                        return
                    }

                    if ((t is EOFException || t is IOException) && reconnectionValid) {
                        attemptReconnect()
                    }
                }
            })
        }
    }

    private fun dispatchMessage(text: String) {
        when {
            text.contains(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME) -> {
                if (LitiainaInstance.enabledDebug) Log.i("LitiainaAndroidSDK", "Dispatching auth update")
                _authMessageLiveData.postValue(Unit)
            }
            text.contains(UPDATE_FILE_LIST_REAL_TIME) -> {
                if (LitiainaInstance.enabledDebug) Log.i("LitiainaAndroidSDK", "Dispatching file list update")
                _fileListMessageLiveData.postValue(Unit)
            }
            else -> if (LitiainaInstance.enabledDebug) Log.w("LitiainaAndroidSDK", "Unhandled message: $text")
        }
    }

    private fun attemptReconnect() {
        val currentAttempts = reconnectAttempts.incrementAndGet()
        if (currentAttempts <= MAX_RECONNECT_ATTEMPTS) {
            val delay = (RECONNECT_DELAY * currentAttempts).coerceAtMost(30L)
            if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Reconnecting... Attempt $currentAttempts in $delay seconds")
            Thread.sleep(delay * 1000)
            connect()
        } else reconnectionValid = false
    }

    fun send(message: String) {
        if (connected && webSocket != null) {
            if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Sending message: $message")
            webSocket?.send(message)
        }
        else {
            if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Queueing message (WebSocket not connected): $message")
            messageQueue.add(message)
        }
    }

    fun close() {
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Manual close triggered")
        isManualClose = true
        reconnectionValid = false
        connected = false
        webSocket?.close(1000, "Closing connection")
        webSocket = null
        uid = null
        channel = null
        messageQueue.clear()
    }

    private fun flushMessageQueue() {
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Flushing message queue: ${messageQueue.size} messages")
        for (message in messageQueue) {
            send(message)
        }
        messageQueue.clear()
    }

    private fun startPing() {
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Starting ping")
        pingRunnable = object : Runnable {
            override fun run() {
                if (connected && webSocket != null) {
                    webSocket?.send("ping")
                }
                pingHandler.postDelayed(this, PING_INTERVAL_MILLIS)
            }
        }
        pingHandler.post(pingRunnable!!)
    }

    private fun stopPing() {
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Stopping ping")
        pingRunnable?.let {
            pingHandler.removeCallbacks(it)
        }
    }

    fun refresh() {
        if (LitiainaInstance.enabledDebug) Log.d("LitiainaAndroidSDK", "Refreshing file/user data via WebSocket")
        send(UPDATE_FILE_LIST_REAL_TIME)
        send(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME)
    }
}
