package com.example.customerangkot.data.datasource

import com.pusher.client.Pusher
import com.pusher.client.connection.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PusherDataSourceImpl(private val pusher: Pusher) : PusherDataSource {
    private val TAG = "PusherDataSourceImpl"

    override suspend fun checkConnection(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (pusher.connection.state == ConnectionState.CONNECTED) {
                    Result.success(true)
                } else {
                    pusher.connect()
                    // Tunggu sebentar untuk memastikan status koneksi
                    Thread.sleep(2000) // Timeout 2 detik
                    if (pusher.connection.state == ConnectionState.CONNECTED) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Pusher tidak tersambung: ${pusher.connection.state}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception("Gagal memeriksa koneksi Pusher: ${e.message}"))
            }
        }
    }

    override fun getPusher(): Pusher {
        return pusher
    }
}