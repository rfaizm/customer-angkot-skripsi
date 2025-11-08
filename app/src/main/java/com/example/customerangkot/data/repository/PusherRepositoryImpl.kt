package com.example.customerangkot.data.repository

import com.example.customerangkot.data.datasource.PusherDataSource
import com.example.customerangkot.domain.repository.PusherRepository
import com.pusher.client.Pusher

class PusherRepositoryImpl(private val pusherDataSource: PusherDataSource) : PusherRepository {
    override suspend fun checkConnection(): Result<Boolean> {
        return pusherDataSource.checkConnection()
    }

    override fun getPusher(): Pusher {
        return pusherDataSource.getPusher()
    }
}