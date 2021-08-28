package com.listocalixto.dailycosmos.data.datastore

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("SimpleDateFormat")
private val sdf = SimpleDateFormat("yyyy-MM-dd")

@Singleton
class APODDataStore @Inject constructor(private val context: Application) {

    private object PreferencesKeys {
        val newStartDate = stringPreferencesKey("new_start_date")
        val referenceDate = stringPreferencesKey("reference_date")
    }

    private val Context.storeLastDate: DataStore<Preferences> by preferencesDataStore(AppConstants.KEY_STORE_LAST_DATE)
    private val Context.storeReferenceDate: DataStore<Preferences> by preferencesDataStore(AppConstants.KEY_STORE_REFERENCE_DATE)

    // STORE LAST DATE //
    suspend fun saveLastDateToDataStore(newStarDate: String) {
        context.storeLastDate.edit { preferences ->
            preferences[PreferencesKeys.newStartDate] = newStarDate
        }
    }

    val readLastDateFromDataStore: Flow<String> = context.storeLastDate.data
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
        context.storeReferenceDate.edit { preferences ->
            preferences[PreferencesKeys.referenceDate] = reference
        }
    }

    val readReferenceDate: Flow<String> = context.storeReferenceDate.data.distinctUntilChanged()
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val referenceDate = preferences[PreferencesKeys.referenceDate] ?: sdf.format(today.time)
            referenceDate
        }

}