package com.litiaina.android.sdk.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.litiaina.android.sdk.constant.Constants
import com.litiaina.android.sdk.constant.Constants.MAX_RECONNECT_ATTEMPTS
import com.litiaina.android.sdk.constant.Constants.PING_INTERVAL_MILLIS
import com.litiaina.android.sdk.constant.Constants.RECONNECT_DELAY
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
    private val _messageLiveData = MutableLiveData<String>()
    val messageLiveData: LiveData<String> get() = _messageLiveData

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
    }

    fun connect() {
        if (connected || reconnectAttempts.get() > MAX_RECONNECT_ATTEMPTS) return

        connected = false
        reconnectionValid = true
        isManualClose = false
        messageQueue.clear()

        webSocket?.close(1000, "Reconnecting")

        apiKey?.let { key ->
            val url = "${Constants.SERVER_WEBSOCKET_URL}?uid=$uid&channel=$channel"
            val request = Request.Builder()
                .url(url)
                .addHeader("authorization", "Bearer $key")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    reconnectAttempts.set(0)
                    connected = true
                    webSocket.send("connected in channel: $channel")
                    flushMessageQueue()
                    startPing()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    _messageLiveData.postValue(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    connected = false
                    reconnectionValid = false
                    webSocket.close(1000, null)
                    stopPing()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    webSocket.cancel()
                    connected = false
                    stopPing()

                    if (response?.code == 401) {
                        Log.e("WebSocketManager", "Unauthorized. Check API key. No further reconnection attempts.")
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

    private fun attemptReconnect() {
        val currentAttempts = reconnectAttempts.incrementAndGet()
        if (currentAttempts <= MAX_RECONNECT_ATTEMPTS) {
            val delay = (RECONNECT_DELAY * currentAttempts).coerceAtMost(30L)
            Thread.sleep(delay * 1000)

            connect()
        } else reconnectionValid = false
    }

    fun send(message: String) {
        if (connected && webSocket != null) {
            webSocket?.send(message)
            _messageLiveData.postValue(message)
        } else messageQueue.add(message)
    }

    fun close() {
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
        for (message in messageQueue) {
            send(message)
        }
        messageQueue.clear()
    }

    private fun startPing() {
        pingRunnable = object : Runnable {
            override fun run() {
                if (connected && webSocket != null) webSocket?.send("ping")
                pingHandler.postDelayed(this, PING_INTERVAL_MILLIS)
            }
        }
        pingHandler.post(pingRunnable!!)
    }

    private fun stopPing() {
        pingRunnable?.let {
            pingHandler.removeCallbacks(it)
        }
    }

    fun refresh() {
        send(UPDATE_FILE_LIST_REAL_TIME)
        send(UPDATE_AUTHENTICATED_USER_DATA_REAL_TIME)
    }

    fun isConnected(): Boolean = connected
}
