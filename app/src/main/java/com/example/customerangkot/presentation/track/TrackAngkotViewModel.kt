package com.example.customerangkot.presentation.track

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.usecase.location.GetPlaceNameUseCase
import com.example.customerangkot.domain.usecase.location.GetRoutesUseCase
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.location.SearchPlaceToCoordinatesUseCase
import com.example.customerangkot.domain.usecase.order.CancelOrderUseCase
import com.example.customerangkot.domain.usecase.order.CreateOrderUseCase
import com.example.customerangkot.domain.usecase.order.GetETAUseCase
import com.example.customerangkot.domain.usecase.trayek.GetAngkotByTrayekIdUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import kotlinx.coroutines.launch

class TrackAngkotViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getPlaceNameUseCase: GetPlaceNameUseCase,
    private val searchPlaceToCoordinatesUseCase: SearchPlaceToCoordinatesUseCase,
    private val getRoutesUseCase: GetRoutesUseCase,
    private val getAngkotByTrayekIdUseCase: GetAngkotByTrayekIdUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val getETAUseCase: GetETAUseCase // [Baru]
) : ViewModel() {

    private val _locationState = MutableLiveData<ResultState<LatLng>>()
    val locationState: LiveData<ResultState<LatLng>> get() = _locationState

    private val _placeNameState = MutableLiveData<ResultState<PlaceNameResponse>>()
    val placeNameState: LiveData<ResultState<PlaceNameResponse>> get() = _placeNameState

    private val _routesState = MutableLiveData<ResultState<RouteResponse>>()
    val routesState: LiveData<ResultState<RouteResponse>> get() = _routesState

    private val _angkotState = MutableLiveData<ResultState<List<DataTrayekJSON>>>()
    val angkotState: LiveData<ResultState<List<DataTrayekJSON>>> get() = _angkotState

    private val _angkotPositions = MutableLiveData<Map<Int, LatLng>>()
    val angkotPositions: LiveData<Map<Int, LatLng>> get() = _angkotPositions

    private val _orderState = MutableLiveData<ResultState<OrderCreatedResponse>>()
    val orderState: LiveData<ResultState<OrderCreatedResponse>> get() = _orderState

    private val _cancelOrderState = MutableLiveData<ResultState<OrderCancelResponse>>() // [Baru]
    val cancelOrderState: LiveData<ResultState<OrderCancelResponse>> get() = _cancelOrderState // [Baru]

    private val _etaState = MutableLiveData<ResultState<GetETAResponse>>()
    val etaState: LiveData<ResultState<GetETAResponse>> get() = _etaState

    // [Baru] State untuk status pesanan
    private val _orderStatus = MutableLiveData<String>("menunggu")
    val orderStatus: LiveData<String> get() = _orderStatus

    var lastLocationType: String? = null

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

    fun getPlaceName(lat: Double, lng: Double, locationType: String) {
        lastLocationType = locationType
        Log.d("TrackAngkotViewModel", "Memanggil getPlaceName untuk $locationType: Lat=$lat, Lng=$lng")
        _placeNameState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getPlaceNameUseCase(lat, lng)
            _placeNameState.value = when {
                result.isSuccess -> {
                    Log.d("TrackAngkotViewModel", "Nama tempat berhasil untuk $locationType: ${result.getOrThrow().data?.placeName}")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal mendapatkan nama tempat"
                    Log.e("TrackAngkotViewModel", "Error untuk $locationType: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }

    fun getRoutes(startLat: Double, startLong: Double, endLat: Double, endLong: Double, routeOption: String) {
        Log.d("TrackAngkotViewModel", "Mengambil rute: startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong, routeOption=$routeOption")
        _routesState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getRoutesUseCase(startLat, startLong, endLat, endLong, routeOption)
            _routesState.value = when {
                result.isSuccess -> {
                    Log.d("TrackAngkotViewModel", "Rute berhasil: ${result.getOrThrow().data?.size} rute")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengambil rute"
                    Log.e("TrackAngkotViewModel", "Error: $errorMessage")
                    ResultState.Error(errorMessage)
                }
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

    fun updateAngkotPosition(angkotId: Int, lat: Double, lng: Double) {
        val currentPositions = _angkotPositions.value?.toMutableMap() ?: mutableMapOf()
        currentPositions[angkotId] = LatLng(lat, lng)
        _angkotPositions.postValue(currentPositions)
        Log.d("TrackAngkotViewModel", "Updated position for Angkot $angkotId: Lat=$lat, Lng=$lng")
    }

    fun createOrder(
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double
    ) {
        _orderState.value = ResultState.Loading
        viewModelScope.launch {
            val result = createOrderUseCase(driverId, startLat, startLong, destinationLat, destinationLong, numberOfPassengers, totalPrice)
            _orderState.value = when {
                result.isSuccess -> {
                    Log.d("TrackAngkotViewModel", "Pesanan berhasil dibuat: orderId=${result.getOrThrow().data?.orderId}")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal membuat pesanan"
                    Log.e("TrackAngkotViewModel", "Error: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }

    // [Baru] Fungsi untuk membatalkan pesanan
    fun cancelOrder(orderId: Int) {
        _cancelOrderState.value = ResultState.Loading
        viewModelScope.launch {
            val result = cancelOrderUseCase(orderId)
            _cancelOrderState.value = when {
                result.isSuccess -> {
                    Log.d("TrackAngkotViewModel", "Pesanan berhasil dibatalkan: orderId=$orderId")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal membatalkan pesanan"
                    Log.e("TrackAngkotViewModel", "Error membatalkan pesanan: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }

    // [Berubah] Fungsi untuk mendapatkan ETA dengan tujuan berdasarkan status
    fun getETA(
        driverLat: Double,
        driverLong: Double,
        pickupLat: Double,
        pickupLong: Double,
        destinationLat: Double, // [Baru]
        destinationLong: Double // [Baru]
    ) {
        _etaState.value = ResultState.Loading
        viewModelScope.launch {
            val result = getETAUseCase(
                driverLat,
                driverLong,
                if (_orderStatus.value == "dijemput") destinationLat else pickupLat,
                if (_orderStatus.value == "dijemput") destinationLong else pickupLong
            )
            _etaState.value = when {
                result.isSuccess -> {
                    Log.d("TrackAngkotViewModel", "ETA berhasil: eta=${result.getOrThrow().data?.eta}, status=${_orderStatus.value}")
                    ResultState.Success(result.getOrThrow())
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Gagal mendapatkan ETA"
                    Log.e("TrackAngkotViewModel", "Error mendapatkan ETA: $errorMessage")
                    ResultState.Error(errorMessage)
                }
            }
        }
    }

    // [Baru] Fungsi untuk memperbarui status pesanan
    fun updateOrderStatus(status: String) {
        _orderStatus.value = status
        Log.d("TrackAngkotViewModel", "Order status updated: $status")
    }
}