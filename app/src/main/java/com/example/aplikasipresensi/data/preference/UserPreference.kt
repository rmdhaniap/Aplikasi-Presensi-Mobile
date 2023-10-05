package com.example.aplikasipresensi.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Suppress("UNREACHABLE_CODE")
class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveToken(token: String) {
        dataStore.edit {
            it[TOKEN_KEY] = token
        }
    }

    suspend fun saveUser(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    fun getToken(): Flow<String> {
        return dataStore.data.map {
            it[TOKEN_KEY] ?: "null"
        }
    }

    suspend fun deleteToken() {
        dataStore.edit { pref ->
            pref.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: UserPreference? = null
        private val TOKEN_KEY = stringPreferencesKey("token")
        fun getInstance(dataStore: DataStore<Preferences>): UserPreference =
            instance ?: synchronized(this) {
                instance ?: UserPreference(dataStore)
            }.also { instance = it }
    }
}