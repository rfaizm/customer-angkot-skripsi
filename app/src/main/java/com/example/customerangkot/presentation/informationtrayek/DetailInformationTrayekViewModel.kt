package com.example.customerangkot.presentation.informationtrayek

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.usecase.trayek.GetAngkotByTrayekIdUseCase
import kotlinx.coroutines.launch

class DetailInformationTrayekViewModel(
    private val getAngkotByTrayekIdUseCase: GetAngkotByTrayekIdUseCase
) : ViewModel() {
    private val _angkotState = MutableLiveData<ResultState<List<DataTrayekJSON>>>()
    val angkotState: LiveData<ResultState<List<DataTrayekJSON>>> get() = _angkotState

    private val _angkotPositions = MutableLiveData<Map<Int, LatLng>>() // Baris 17: Tambahkan LiveData untuk posisi
    val angkotPositions: LiveData<Map<Int, LatLng>> get() = _angkotPositions

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

    fun updateAngkotPosition(angkotId: Int, lat: Double, lng: Double) {
        val currentPositions = _angkotPositions.value?.toMutableMap() ?: mutableMapOf()
        currentPositions[angkotId] = LatLng(lat, lng)
        _angkotPositions.postValue(currentPositions) // Baris 32: Gunakan postValue
        Log.d("DetailInformationTrayekViewModel", "Updated position for Angkot $angkotId: Lat=$lat, Lng=$lng")
    }
}