package com.example.customerangkot.presentation.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.usecase.auth.LogoutUseCase
import com.example.customerangkot.domain.usecase.user.GetHistoryUseCase
import com.example.customerangkot.domain.usecase.user.GetProfileUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _logoutState = MutableLiveData<ResultState<LogoutResponse>>()
    val logoutState: LiveData<ResultState<LogoutResponse>> get() = _logoutState

    private val _historyState = MutableLiveData<ResultState<HistoryResponse>>()
    val historyState: LiveData<ResultState<HistoryResponse>> get() = _historyState

    private val _getProfile = MutableLiveData<ResultState<GetProfileResponse>>()
    val getProfile: LiveData<ResultState<GetProfileResponse>> get() = _getProfile

    fun logout() {
        _logoutState.value = ResultState.Loading
        viewModelScope.launch {
            val result = logoutUseCase()
            _logoutState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal logout")
            }
        }
    }

    fun getHistory() {
        _historyState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getHistoryUseCase()
            _historyState.value = when {
                result.isSuccess -> {
                    Log.d("ProfileViewModel", "Riwayat berhasil diambil: ${result.getOrNull()?.message}")
                    ResultState.Success(result.getOrNull()!!)
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengambil riwayat"
                    Log.e("ProfileViewModel", "Error: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }

    fun getProfile() {
        _getProfile.value = ResultState.Loading
        viewModelScope.launch {
            val result = getProfileUseCase()
            _getProfile.value = when {
                result.isSuccess -> {
                    Log.d("ProfileViewModel", "Riwayat berhasil diambil: ${result.getOrNull()?.message}")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengambil riwayat"
                    Log.e("ProfileViewModel", "Error: $errorMessage")
                    ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal mengambil data")
                }
            }
        }
    }
}