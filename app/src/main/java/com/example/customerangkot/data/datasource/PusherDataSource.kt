package com.example.customerangkot.data.datasource

import com.pusher.client.Pusher

interface PusherDataSource {
    suspend fun checkConnection(): Result<Boolean>
    fun getPusher(): Pusher
}