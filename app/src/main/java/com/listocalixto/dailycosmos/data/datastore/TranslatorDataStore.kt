package com.listocalixto.dailycosmos.data.datastore

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.listocalixto.dailycosmos.application.AppConstants
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslatorDataStore @Inject constructor(private val context: Application) {

    private object PreferencesKeys {
        val isDownloaded = intPreferencesKey("is_model_translator_downloaded")
    }

    private val Context.storeIsDownloaded: DataStore<Preferences> by preferencesDataStore(
        AppConstants.KEY_STORE_IS_MODEL_DOWNLOADED
    )

    suspend fun saveValue(value: Int) {
        context.storeIsDownloaded.edit { preferences ->
            preferences[PreferencesKeys.isDownloaded] = value
        }
        Log.d("DataStore", "The answer has been saved: $value")
    }

    val readValue: Flow<Int> = context.storeIsDownloaded.data.distinctUntilChanged()
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