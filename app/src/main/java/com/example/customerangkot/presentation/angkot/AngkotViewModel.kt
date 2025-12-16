package com.example.customerangkot.presentation.angkot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.trayek.GetAngkotByTrayekIdUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import kotlinx.coroutines.launch

class AngkotViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getClosestTrayekUseCase: GetClosestTrayekUseCase,
    private val getAngkotByTrayekIdUseCase: GetAngkotByTrayekIdUseCase
) : ViewModel() {
    private val _locationState = MutableLiveData<ResultState<LatLng>>()
    val locationState: LiveData<ResultState<LatLng>> get() = _locationState

    private val _selectedTrayekId = MutableLiveData<Int?>()
    val selectedTrayekId: LiveData<Int?> get() = _selectedTrayekId

    private val _angkotState = MutableLiveData<ResultState<List<DataTrayekJSON>>>()
    val angkotState: LiveData<ResultState<List<DataTrayekJSON>>> get() = _angkotState

    private val _angkotPositions = MutableLiveData<Map<Int, LatLng>>()
    val angkotPositions: LiveData<Map<Int, LatLng>> get() = _angkotPositions

    private val _angkotPlatNomor = mutableMapOf<Int, String>()

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


    fun getAngkotByTrayekId(lat: Double, lng: Double, trayekId: Int) {
        _angkotState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getAngkotByTrayekIdUseCase(lat, lng, trayekId)
            _angkotState.value = when {
                result.isSuccess -> ResultState.Success(result.getOrThrow())
                else -> ResultState.Error(result.exceptionOrNull()?.message ?: "Gagal mengambil data angkot")
            }
        }
    }

    fun setSelectedTrayek(trayekId: Int?) {
        _selectedTrayekId.value = trayekId
    }

    fun clearAngkotState() {
        _angkotState.value = ResultState.Success(emptyList())
    }

    fun updateAngkotPosition(angkotId: Int, lat: Double, lng: Double, platNomor: String? = null) {
        val currentPositions = _angkotPositions.value?.toMutableMap() ?: mutableMapOf()
        currentPositions[angkotId] = LatLng(lat, lng)
        _angkotPositions.postValue(currentPositions)

        // Kirim platNomor ke observer
        _angkotPlatNomor[angkotId] = (platNomor ?: _angkotPlatNomor[angkotId]).toString()
    }
}