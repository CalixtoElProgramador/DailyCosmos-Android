package com.listocalixto.dailycosmos.data.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

const val STORE_DIALOG_CAUTION_OPEN_IMAGE = "preferences_04"
const val STORE_IS_THE_FIRST_SEARCH = "preferences_is_the_first_search"

class UtilsDataStore(context: Context) {

    private object PreferencesKeys {
        val isAccepted = preferencesKey<Int>("is_dialog_caution_open_image_accepted")
        val isFirstSearch = preferencesKey<Int>("is_the_first_search")
    }

    private val storeIsAccepted: DataStore<Preferences> =
        context.createDataStore(name = STORE_DIALOG_CAUTION_OPEN_IMAGE)
    private val isFirstSearch: DataStore<Preferences> =
        context.createDataStore(name = STORE_IS_THE_FIRST_SEARCH)

    suspend fun saveValue(value: Int) {
        storeIsAccepted.edit { preferences -> preferences[PreferencesKeys.isAccepted] = value }
        Log.d("DataStore", "The answer has been saved: $value")
    }

    val readValue: Flow<Int> = storeIsAccepted.data.distinctUntilChanged()
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
        isFirstSearch.edit { preferences -> preferences[PreferencesKeys.isFirstSearch] = value }
        Log.d("DataStore", "isFirstSearch: $value")
    }

    val readValueSearch: Flow<Int> = isFirstSearch.data.distinctUntilChanged()
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
}