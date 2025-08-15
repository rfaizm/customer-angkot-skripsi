package com.example.customerangkot.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {
    private val _loginState = MutableLiveData<ResultState<Pair<User, Passenger>>>()
    val loginState: LiveData<ResultState<Pair<User, Passenger>>> get() = _loginState

    fun login(email: String, password: String) {
        _loginState.value = ResultState.Loading
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            _loginState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal login")
            }
        }
    }
}