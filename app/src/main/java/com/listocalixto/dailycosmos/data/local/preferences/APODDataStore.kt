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
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val STORE_LAST_DATE = "preferences_01"
const val STORE_LIST_SIZE = "preferences_02"

@SuppressLint("SimpleDateFormat")
private val sdf = SimpleDateFormat("yyyy-MM-dd")

class APODDataStore(context: Context) {

    private object PreferencesKeys {
        val newStartDate = preferencesKey<String>("new_star_date")
        val sizeList = preferencesKey<Int>("size_list")
    }

    private val storeLastDate: DataStore<Preferences> =
        context.createDataStore(name = STORE_LAST_DATE)
    private val storeListSize: DataStore<Preferences> =
        context.createDataStore(name = STORE_LIST_SIZE)

    // STORE LAST DATE //
    suspend fun saveLastDateToDataStore(newStarDate: String) {
        storeLastDate.edit { preferences ->
            preferences[PreferencesKeys.newStartDate] = newStarDate
        }
        Log.d("DataStore", "The date has been saved: $newStarDate")
    }

    val readLastDateFromDataStore: Flow<String> = storeLastDate.data
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
            val newStartDate =
                preferences[PreferencesKeys.newStartDate] ?: sdf.format(startDate.time)
            Log.d("DataStore", "The date is being read: $newStartDate")
            newStartDate
        }
}