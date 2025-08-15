package com.example.customerangkot.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerUseCase: RegisterUseCase) : ViewModel() {
    private val _registerState = MutableLiveData<ResultState<Pair<User, Passenger>>>()
    val registerState: LiveData<ResultState<Pair<User, Passenger>>> get() = _registerState

    fun register(name: String, email: String, noHp: String, password: String) {
        _registerState.value = ResultState.Loading
        viewModelScope.launch {
            val result = registerUseCase(name, email, noHp, password)
            _registerState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal registrasi")
            }
        }
    }
}