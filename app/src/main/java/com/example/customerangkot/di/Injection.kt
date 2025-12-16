package com.example.customerangkot.di

import android.content.Context
import com.example.customerangkot.data.api.ApiConfig
import com.example.customerangkot.data.datasource.LocationDataSourceImpl
import com.example.customerangkot.data.datasource.OrderDataSourceImpl
import com.example.customerangkot.data.datasource.PusherDataSource
import com.example.customerangkot.data.datasource.PusherDataSourceImpl
import com.example.customerangkot.data.datasource.TrayekDataSourceImpl
import com.example.customerangkot.data.datasource.UserDataSourceImpl
import com.example.customerangkot.data.repository.LocationRepositoryImpl
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.data.preference.dataStore
import com.example.customerangkot.data.repository.OrderRepositoryImpl
import com.example.customerangkot.data.repository.PusherRepositoryImpl
import com.example.customerangkot.data.repository.TrayekRepositoryImpl
import com.example.customerangkot.data.repository.UserRepositoryImpl
import com.example.customerangkot.domain.repository.LocationRepository
import com.example.customerangkot.domain.repository.OrderRepository
import com.example.customerangkot.domain.repository.PusherRepository
import com.example.customerangkot.domain.repository.TrayekRepository
import com.example.customerangkot.domain.repository.UserRepository
import com.example.customerangkot.domain.usecase.auth.LoginUseCase
import com.example.customerangkot.domain.usecase.auth.LogoutUseCase
import com.example.customerangkot.domain.usecase.auth.RegisterUseCase
import com.example.customerangkot.domain.usecase.location.CheckPusherConnectionUseCase
import com.example.customerangkot.domain.usecase.location.GetPlaceNameUseCase
import com.example.customerangkot.domain.usecase.location.GetRoutesUseCase
import com.example.customerangkot.domain.usecase.location.GetUserLocationUseCase
import com.example.customerangkot.domain.usecase.location.SearchPlaceToCoordinatesUseCase
import com.example.customerangkot.domain.usecase.order.CancelOrderUseCase
import com.example.customerangkot.domain.usecase.order.CreateOrderUseCase
import com.example.customerangkot.domain.usecase.order.GetETAUseCase
import com.example.customerangkot.domain.usecase.trayek.GetAngkotByTrayekIdUseCase
import com.example.customerangkot.domain.usecase.trayek.GetClosestTrayekUseCase
import com.example.customerangkot.domain.usecase.trayek.GetDriverIdWithAngkotIdUseCase
import com.example.customerangkot.domain.usecase.user.GetHistoryUseCase
import com.example.customerangkot.domain.usecase.user.GetProfileUseCase
import com.example.customerangkot.domain.usecase.user.GetSaldoUseCase
import com.example.customerangkot.domain.usecase.user.TopUpUseCase
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions

object Injection {
    private fun providePusherDataSource(): PusherDataSource {
        val options = PusherOptions().setCluster("ap1")
        val pusher = Pusher("d1373b327727bf1ce9cf", options)
        return PusherDataSourceImpl(pusher)
    }

    private fun providePusherRepository(): PusherRepository {
        return PusherRepositoryImpl(providePusherDataSource())
    }
    private fun provideUserRepository(context: Context): UserRepository {
        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(context.dataStore)
        val userDataSource = UserDataSourceImpl(apiService, userPreference)
        return UserRepositoryImpl(userDataSource)
    }

    private fun provideLocationRepository(context: Context): LocationRepository {
        val apiService = ApiConfig.getApiService()
        val locationDataSource = LocationDataSourceImpl(context, apiService)
        return LocationRepositoryImpl(locationDataSource)
    }

    private fun provideTrayekRepository(context: Context): TrayekRepository {
        val apiService = ApiConfig.getApiService()
        val trayekDataSource = TrayekDataSourceImpl(apiService)
        return TrayekRepositoryImpl(trayekDataSource)
    }

    private fun provideOrderRepository(context: Context): OrderRepository {
        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(context.dataStore)
        val orderDataSource = OrderDataSourceImpl(apiService, userPreference)
        return OrderRepositoryImpl(orderDataSource)
    }

    fun provideRegisterUseCase(context: Context): RegisterUseCase {
        return RegisterUseCase(provideUserRepository(context))
    }

    fun provideLoginUseCase(context: Context): LoginUseCase {
        return LoginUseCase(provideUserRepository(context))
    }

    fun provideLogoutUseCase(context: Context): LogoutUseCase {
        return LogoutUseCase(provideUserRepository(context))
    }

    fun provideGetUserLocationUseCase(context: Context): GetUserLocationUseCase {
        return GetUserLocationUseCase(provideLocationRepository(context))
    }

    fun provideGetClosestTrayekUseCase(context: Context): GetClosestTrayekUseCase {
        return GetClosestTrayekUseCase(provideTrayekRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideGetAngkotByTrayekIdUseCase(context: Context): GetAngkotByTrayekIdUseCase {
        return GetAngkotByTrayekIdUseCase(provideTrayekRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideGetPlaceNameUseCase(context: Context): GetPlaceNameUseCase {
        return GetPlaceNameUseCase(provideLocationRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideSearchPlaceToCoordinatesUseCase(context: Context): SearchPlaceToCoordinatesUseCase {
        return SearchPlaceToCoordinatesUseCase(provideLocationRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideGetRoutesUseCase(context: Context): GetRoutesUseCase {
        return GetRoutesUseCase(provideLocationRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideCreateOrderUseCase(context: Context): CreateOrderUseCase {
        return CreateOrderUseCase(provideOrderRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideTopUpUseCase(context: Context): TopUpUseCase {
        return TopUpUseCase(provideUserRepository(context))
    }

    fun provideGetSaldoUseCase(context: Context): GetSaldoUseCase {
        return GetSaldoUseCase(provideUserRepository(context))
    }

    fun provideGetHistoryUseCase(context: Context): GetHistoryUseCase {
        return GetHistoryUseCase(provideUserRepository(context))
    }

    fun provideGetProfileUseCase(context: Context): GetProfileUseCase {
        return GetProfileUseCase(provideUserRepository(context))
    }

    fun provideCancelOrderUseCase(context: Context): CancelOrderUseCase {
        return CancelOrderUseCase(provideOrderRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideGetETAUseCase(context: Context): GetETAUseCase {
        return GetETAUseCase(provideOrderRepository(context), UserPreference.getInstance(context.dataStore))
    }

    fun provideCheckPusherConnectionUseCase(): CheckPusherConnectionUseCase {
        return CheckPusherConnectionUseCase(providePusherRepository())
    }

    fun provideGetDriverIdWithAngkotIdUseCase(context: Context): GetDriverIdWithAngkotIdUseCase {
        return GetDriverIdWithAngkotIdUseCase(provideTrayekRepository(context), UserPreference.getInstance(context.dataStore))
    }
//    fun proviceGetCheckActiveOrderUseCase(context: Context): GetCheckActiveOrder {
//        return GetCheckActiveOrder(provideOrderRepository(context), UserPreference.getInstance(context.dataStore))
//    }
}