package com.listocalixto.dailycosmos.presentation.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.data.local.preferences.UtilsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UtilsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = UtilsDataStore(application)

    val readValue = dataStore.readValue.asLiveData()

    fun saveValue(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.saveValue(value)
    }

    val readValueSearch = dataStore.readValueSearch.asLiveData()
    fun saveValueSearch(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.saveValueSearch(value)
    }

    val readValueFirstTime = dataStore.readValueFirstTime.asLiveData()
    fun saveValueFirstTime(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.saveValueFirstTime(value)
    }

}