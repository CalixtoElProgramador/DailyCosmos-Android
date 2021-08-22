package com.listocalixto.dailycosmos.data.local.preferences

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.*

const val STORE_LAST_DATE = "preferences_01"
const val STORE_REFERENCE_DATE = "preferences_store_reference_date"

@SuppressLint("SimpleDateFormat")
private val sdf = SimpleDateFormat("yyyy-MM-dd")

class APODDataStore(context: Context) {

    private object PreferencesKeys {
        val newStartDate = preferencesKey<String>("new_star_date")
        val referenceDate = preferencesKey<String>("reference_date")
    }

    private val storeLastDate: DataStore<Preferences> =
        context.createDataStore(name = STORE_LAST_DATE)
    private val storeReferenceDate: DataStore<Preferences> =
        context.createDataStore(name = STORE_REFERENCE_DATE)

    // STORE LAST DATE //
    suspend fun saveLastDateToDataStore(newStarDate: String) {
        storeLastDate.edit { preferences ->
            preferences[PreferencesKeys.newStartDate] = newStarDate
        }
    }

    val readLastDateFromDataStore: Flow<String> = storeLastDate.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStore", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val today: Calendar = Calendar.getInstance()
            val startDate: Calendar = Calendar.getInstance().apply {
                set(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DATE)
                )
                add(Calendar.DATE, -10)
            }
            val newStartDate = preferences[PreferencesKeys.newStartDate] ?: sdf.format(startDate.time)
            newStartDate
        }

    suspend fun saveReferenceDate(reference: String) {
        storeReferenceDate.edit { preferences ->
            preferences[PreferencesKeys.referenceDate] = reference
        }
    }

    val readReferenceDate: Flow<String> = storeReferenceDate.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val reference: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                add(Calendar.DATE, -1)
            }
            val referenceDate = preferences[PreferencesKeys.referenceDate] ?: sdf.format(reference.time)
            referenceDate
        }

}