package com.example.customerangkot.presentation.topup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.usecase.user.TopUpUseCase
import kotlinx.coroutines.launch

class TopUpViewModel(
    private val topUpUseCase: TopUpUseCase
) : ViewModel() {

    private val _topUpState = MutableLiveData<ResultState<TopUpResponse>>()
    val topUpState: LiveData<ResultState<TopUpResponse>> get() = _topUpState

    fun topUp(nominal: Int) {
        _topUpState.value = ResultState.Loading
        viewModelScope.launch {
            val result = topUpUseCase(nominal)
            _topUpState.value = when {
                result.isSuccess -> {
                    Log.d("TopUpViewModel", "Top-up berhasil: ${result.getOrNull()?.message}")
                    ResultState.Success(result.getOrNull()!!)
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal melakukan top-up"
                    Log.e("TopUpViewModel", "Error: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }
}