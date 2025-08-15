package com.example.customerangkot.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import com.example.customerangkot.domain.usecase.user.GetSaldoUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getClosestTrayekUseCase: GetClosestTrayekUseCase,
    private val getSaldoUseCase: GetSaldoUseCase
) : ViewModel() {
    private val _locationState = MutableLiveData<ResultState<LatLng>>()
    val locationState: LiveData<ResultState<LatLng>> get() = _locationState

    private val _trayekState = MutableLiveData<ResultState<List<TrayekItem>>>()
    val trayekState: LiveData<ResultState<List<TrayekItem>>> get() = _trayekState

    private val _allTrayekState = MutableLiveData<ResultState<List<DataTrayekJSON>>>()
    val allTrayekState: LiveData<ResultState<List<DataTrayekJSON>>> get() = _allTrayekState

    private val _selectedAngkotIds = MutableLiveData<List<Int>?>()
    val selectedAngkotIds: LiveData<List<Int>?> get() = _selectedAngkotIds

    private val _angkotPositions = MutableLiveData<Map<Int, LatLng>>()
    val angkotPositions: LiveData<Map<Int, LatLng>> get() = _angkotPositions

    private val _getSaldo = MutableLiveData<ResultState<TopUpResponse>>()
    val getSaldo: LiveData<ResultState<TopUpResponse>> get() = _getSaldo

    fun getUserLocation() {
        _locationState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getUserLocationUseCase()
            _locationState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal mendapatkan lokasi")
            }
        }
    }

    fun getClosestTrayek(lat: Double, lng: Double) {
        _trayekState.value = ResultState.Loading
        _allTrayekState.value = ResultState.Loading
        viewModelScope.launch {
            val trayekResult = getClosestTrayekUseCase.getUniqueTrayeks(lat, lng)
            _trayekState.value = when {
                trayekResult.isSuccess -> ResultState.Success(trayekResult.getOrThrow())
                else -> ResultState.Error(trayekResult.exceptionOrNull()?.message ?: "Gagal mengambil trayek terdekat")
            }
            val allTrayekResult = getClosestTrayekUseCase.getAllTrayeks(lat, lng)
            _allTrayekState.value = when {
                allTrayekResult.isSuccess -> ResultState.Success(allTrayekResult.getOrThrow())
                else -> ResultState.Error(allTrayekResult.exceptionOrNull()?.message ?: "Gagal mengambil trayek terdekat")
            }
        }
    }

    fun setSelectedAngkotIds(angkotIds: List<Int>?) {
        _selectedAngkotIds.value = angkotIds
    }

    fun updateAngkotPosition(angkotId: Int, lat: Double, lng: Double) {
        val currentPositions = _angkotPositions.value?.toMutableMap() ?: mutableMapOf()
        currentPositions[angkotId] = LatLng(lat, lng)
        _angkotPositions.postValue(currentPositions) // Baris 67: Ganti setValue dengan postValue
        Log.d("HomeViewModel", "Updated position for Angkot $angkotId: Lat=$lat, Lng=$lng")
    }

    fun getSaldo() {
        _getSaldo.value = ResultState.Loading
        viewModelScope.launch {
            val result = getSaldoUseCase()
            _getSaldo.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal mengambil saldo")
            }
        }
    }
}