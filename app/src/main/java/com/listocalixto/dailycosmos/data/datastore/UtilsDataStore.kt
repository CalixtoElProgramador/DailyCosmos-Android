package com.listocalixto.dailycosmos.data.datastore

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.listocalixto.dailycosmos.application.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UtilsDataStore @Inject constructor(private val context: Application) {

    private object PreferencesKeys {
        val isAccepted = intPreferencesKey("is_dialog_caution_open_image_accepted")
        val isFirstSearch = intPreferencesKey("is_the_first_search")
        val isFirstTime = intPreferencesKey("is_the_first_time_on_the_app")
        val isFirstTimeGetResults = intPreferencesKey("is_the_first_time_get_results")
        val darkThemeMode = intPreferencesKey("is_dark_theme_activated")
    }

    private val Context.storeIsAccepted: DataStore<Preferences> by preferencesDataStore(AppConstants.KEY_STORE_DIALOG_CAUTION_OPEN_IMAGE)
    private val Context.isFirstSearch: DataStore<Preferences> by preferencesDataStore(AppConstants.KEY_STORE_IS_THE_FIRST_SEARCH)
    private val Context.isFirstTime: DataStore<Preferences> by preferencesDataStore(AppConstants.KEY_STORE_IS_THE_FIRST_TIME_ON_THE_APP)
    private val Context.isFirstTimeGetResults: DataStore<Preferences> by preferencesDataStore(
        AppConstants.KEY_STORE_IS_THE_FIRST_TIME_GET_RESULTS
    )
    private val Context.isDarkThemeActivated: DataStore<Preferences> by preferencesDataStore(
        AppConstants.KEY_STORE_IS_DARK_THEME_ACTIVATED
    )

    suspend fun setDarkThemeMode(answer: Int) {
        context.isDarkThemeActivated.edit { preferences ->
            preferences[PreferencesKeys.darkThemeMode] = answer
        }
    }

    val getDarkThemeMode: Flow<Int> = context.isDarkThemeActivated.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val darkTheme = preferences[PreferencesKeys.darkThemeMode] ?: 0
            darkTheme
        }

    suspend fun saveValue(value: Int) {
        context.storeIsAccepted.edit { preferences ->
            preferences[PreferencesKeys.isAccepted] = value
        }
        Log.d("DataStore", "The answer has been saved: $value")
    }

    val readValue: Flow<Int> = context.storeIsAccepted.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val value = preferences[PreferencesKeys.isAccepted] ?: 0
            value
        }

    suspend fun saveValueSearch(value: Int) {
        context.isFirstSearch.edit { preferences ->
            preferences[PreferencesKeys.isFirstSearch] = value
        }
        Log.d("DataStore", "isFirstSearch: $value")
    }

    val readValueSearch: Flow<Int> = context.isFirstSearch.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val value = preferences[PreferencesKeys.isFirstSearch] ?: 0
            value
        }

    suspend fun saveValueFirstTime(value: Int) {
        context.isFirstTime.edit { preferences -> preferences[PreferencesKeys.isFirstTime] = value }
    }

    val readValueFirstTime: Flow<Int> = context.isFirstTime.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val value = preferences[PreferencesKeys.isFirstTime] ?: 0
            value
        }

    suspend fun saveValueFirstTimeGetResults(value: Int) {
        context.isFirstTimeGetResults.edit { preferences ->
            preferences[PreferencesKeys.isFirstTimeGetResults] = value
        }
    }

    val readValueFirstTimeGetResults: Flow<Int> =
        context.isFirstTimeGetResults.data.distinctUntilChanged()
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val value = preferences[PreferencesKeys.isFirstTimeGetResults] ?: 0
                value
            }

}