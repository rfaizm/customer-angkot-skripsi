package com.example.customerangkot.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.entity.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference(private val dataStore: DataStore<Preferences>) {


    suspend fun saveSession(user: User, token : String, noHp: String, saldo: Int) {
        dataStore.edit { preferences ->
            preferences[ID_KEY] = user.id
            preferences[EMAIL_KEY] = user.email
            preferences[NAME_KEY] = user.name
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = user.role
            preferences[NO_HP_KEY] = noHp
            preferences[IS_LOGIN_KEY] = true
        }
    }

    suspend fun updateSession(user: User, noHp: String) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = user.name
            preferences[EMAIL_KEY] = user.email
            preferences[NO_HP_KEY] = noHp
        }
    }

    suspend fun updateSaldo(saldo: Int) {
        dataStore.edit { preferences ->
            preferences[SALDO_KEY] = saldo
        }
    }


    // TODO : Bagaimana cara agar return bukan hanya class User tapi juga return parameter lain seperti noHp, dan isLogin?
    fun getSession(): Flow<UserSession> {
        return dataStore.data.map { preferences ->
            UserSession(
                user = User(
                    id = preferences[ID_KEY] ?: 0,
                    email = preferences[EMAIL_KEY] ?: "",
                    name = preferences[NAME_KEY] ?: "",
                    role = preferences[ROLE_KEY] ?: ""
                ),
                token = preferences[TOKEN_KEY] ?: "",
                noHp = preferences[NO_HP_KEY] ?: "",
                saldo = preferences[SALDO_KEY] ?: 0,
                isLogin = preferences[IS_LOGIN_KEY] ?: false
            )
        }
    }


    fun getLogin(): Boolean? {
        return runBlocking {
            dataStore.data.first()[IS_LOGIN_KEY]
        }
    }

    fun getName(): String? {
        return runBlocking {
            val fullName = dataStore.data.first()[NAME_KEY]
            fullName?.split(" ")?.firstOrNull()
        }
    }


    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    fun getAuthToken(): String? {
        return runBlocking {
            dataStore.data.first()[TOKEN_KEY]
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val ID_KEY = intPreferencesKey("userId")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NAME_KEY = stringPreferencesKey("name")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val NO_HP_KEY = stringPreferencesKey("noHp")
        private val SALDO_KEY = intPreferencesKey("saldo")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}