package com.example.customerangkot.presentation.angkot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import kotlinx.coroutines.launch

class AngkotViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getClosestTrayekUseCase: GetClosestTrayekUseCase
) : ViewModel() {
    private val _locationState = MutableLiveData<ResultState<LatLng>>()
    val locationState: LiveData<ResultState<LatLng>> get() = _locationState

    private val _trayekState = MutableLiveData<ResultState<List<TrayekItem>>>()
    val trayekState: LiveData<ResultState<List<TrayekItem>>> get() = _trayekState

    private val _selectedAngkotIds = MutableLiveData<List<Int>?>()
    val selectedAngkotIds: LiveData<List<Int>?> get() = _selectedAngkotIds

    private val _angkotPositions = MutableLiveData<Map<Int, LatLng>>()
    val angkotPositions: LiveData<Map<Int, LatLng>> get() = _angkotPositions

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
        viewModelScope.launch {
            val result = getClosestTrayekUseCase.getUniqueTrayeks(lat, lng)
            _trayekState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal mengambil trayek terdekat")
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
}