package com.example.customerangkot.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.customerangkot.domain.usecase.auth.LoginUseCase
import com.example.customerangkot.domain.usecase.auth.LogoutUseCase
import com.example.customerangkot.domain.usecase.auth.RegisterUseCase
import com.example.customerangkot.domain.usecase.location.GetPlaceNameUseCase
import com.example.customerangkot.domain.usecase.location.GetRoutesUseCase
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.location.SearchPlaceToCoordinatesUseCase
import com.example.customerangkot.domain.usecase.order.CancelOrderUseCase
import com.example.customerangkot.domain.usecase.order.CreateOrderUseCase
import com.example.customerangkot.domain.usecase.order.GetETAUseCase
import com.example.customerangkot.domain.usecase.trayek.GetAngkotByTrayekIdUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import com.example.customerangkot.domain.usecase.user.GetHistoryUseCase
import com.example.customerangkot.domain.usecase.user.GetProfileUseCase
import com.example.customerangkot.domain.usecase.user.GetSaldoUseCase
import com.example.customerangkot.domain.usecase.user.TopUpUseCase
import com.example.customerangkot.presentation.angkot.AngkotViewModel
import com.example.customerangkot.presentation.home.HomeViewModel
import com.example.customerangkot.presentation.informationtrayek.DetailInformationTrayekViewModel
import com.example.customerangkot.presentation.login.LoginViewModel
import com.example.customerangkot.presentation.profile.ProfileViewModel
import com.example.customerangkot.presentation.register.RegisterViewModel
import com.example.customerangkot.presentation.topup.TopUpViewModel
import com.example.customerangkot.presentation.track.TrackAngkotViewModel


class ViewModelFactory private constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getClosestTrayekUseCase: GetClosestTrayekUseCase,
    private val getAngkotByTrayekIdUseCase: GetAngkotByTrayekIdUseCase,
    private val getPlaceNameUseCase: GetPlaceNameUseCase,
    private val searchPlaceToCoordinatesUseCase: SearchPlaceToCoordinatesUseCase,
    private val getRoutesUseCase: GetRoutesUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val topUpUseCase: TopUpUseCase,
    private val getSaldoUseCase: GetSaldoUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val getETAUseCase: GetETAUseCase // [Baru]
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(registerUseCase) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(loginUseCase) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(logoutUseCase, getHistoryUseCase, getProfileUseCase) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(getUserLocationUseCase, getClosestTrayekUseCase, getSaldoUseCase) as T
            }
            modelClass.isAssignableFrom(AngkotViewModel::class.java) -> {
                AngkotViewModel(getUserLocationUseCase, getClosestTrayekUseCase) as T
            }
            modelClass.isAssignableFrom(DetailInformationTrayekViewModel::class.java) -> {
                DetailInformationTrayekViewModel(getAngkotByTrayekIdUseCase) as T
            }
            modelClass.isAssignableFrom(TrackAngkotViewModel::class.java) -> {
                TrackAngkotViewModel(
                    getUserLocationUseCase,
                    getPlaceNameUseCase,
                    searchPlaceToCoordinatesUseCase,
                    getRoutesUseCase,
                    getAngkotByTrayekIdUseCase,
                    createOrderUseCase,
                    cancelOrderUseCase,
                    getETAUseCase // [Baru]
                ) as T
            }
            modelClass.isAssignableFrom(TopUpViewModel::class.java) -> {
                TopUpViewModel(topUpUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideRegisterUseCase(context),
                    Injection.provideLoginUseCase(context),
                    Injection.provideLogoutUseCase(context),
                    Injection.provideGetUserLocationUseCase(context),
                    Injection.provideGetClosestTrayekUseCase(context),
                    Injection.provideGetAngkotByTrayekIdUseCase(context),
                    Injection.provideGetPlaceNameUseCase(context),
                    Injection.provideSearchPlaceToCoordinatesUseCase(context),
                    Injection.provideGetRoutesUseCase(context),
                    Injection.provideCreateOrderUseCase(context),
                    Injection.provideTopUpUseCase(context),
                    Injection.provideGetSaldoUseCase(context),
                    Injection.provideGetHistoryUseCase(context),
                    Injection.provideGetProfileUseCase(context),
                    Injection.provideCancelOrderUseCase(context),
                    Injection.provideGetETAUseCase(context) // [Baru]
                )
            }.also { instance = it }
    }
}