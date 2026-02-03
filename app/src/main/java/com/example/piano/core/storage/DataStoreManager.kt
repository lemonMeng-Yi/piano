package com.example.piano.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * DataStoreManager 提供对 DataStore 偏好的底层访问。
 * 处理核心数据持久化操作，包含错误处理和默认值。
 */
class DataStoreManager private constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private const val DATASTORE_NAME = "app_preferences"

        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreManager(context.dataStore).also { INSTANCE = it }
            }

        private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defaultValue
            }
            .catch { emit(defaultValue) }

    fun getLong(key: String, defaultValue: Long = 0L): Flow<Long> =
        dataStore.data
            .map { preferences ->
                preferences[longPreferencesKey(key)] ?: defaultValue
            }
            .catch { emit(defaultValue) }

    fun getString(key: String, defaultValue: String = ""): Flow<String> =
        dataStore.data
            .map { preferences ->
                preferences[stringPreferencesKey(key)] ?: defaultValue
            }
            .catch { emit(defaultValue) }

    suspend fun setBoolean(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    suspend fun setLong(key: String, value: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }

    suspend fun setString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    suspend fun removeString(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }

    suspend fun removeLong(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(longPreferencesKey(key))
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
