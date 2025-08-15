package com.example.customerangkot.domain.entity

data class UserSession(
    val user: User,
    val token: String,
    val noHp: String,
    val saldo: Int,
    val isLogin: Boolean
)