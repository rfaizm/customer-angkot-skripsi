package com.example.customerangkot.domain.repository

import com.pusher.client.Pusher

interface PusherRepository {
    suspend fun checkConnection(): Result<Boolean>
    fun getPusher(): Pusher
}