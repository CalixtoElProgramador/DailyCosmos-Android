package com.listocalixto.dailycosmos.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

const val STORE_IS_MODEL_DOWNLOADED = "preferences_03"

class TranslatorDataStore(context: Context) {

    private object PreferencesKeys {
        val isDownloaded = preferencesKey<Int>("is_model_translator_downloaded")
    }

    private val storeIsDownloaded: DataStore<Preferences> =
        context.createDataStore(name = STORE_IS_MODEL_DOWNLOADED)

    suspend fun saveValue(value: Int) {
        storeIsDownloaded.edit { preferences ->
            preferences[PreferencesKeys.isDownloaded] = value
        }
        Log.d("DataStore", "The answer has been saved: $value")
    }

    val readValue: Flow<Int> = storeIsDownloaded.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStoreTradcutor", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val value = preferences[PreferencesKeys.isDownloaded] ?: 0
            Log.d("DataStore", "The value is $value")
            value
        }
}