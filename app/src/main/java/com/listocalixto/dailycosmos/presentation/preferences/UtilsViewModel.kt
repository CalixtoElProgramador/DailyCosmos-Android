package com.listocalixto.dailycosmos.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.data.datastore.UtilsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UtilsViewModel @Inject constructor(private val dataStore: UtilsDataStore) : ViewModel() {

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

    val readValueFirstTimeGetResults = dataStore.readValueFirstTimeGetResults.asLiveData()
    fun saveValueFirstTimeGetResults(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.saveValueFirstTimeGetResults(value)
    }

    val getDarkThemeMode = dataStore.getDarkThemeMode.asLiveData()
    fun setDarkThemeMode(mode: Int) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setDarkThemeMode(mode)
    }

    val isDialogShowAgain = dataStore.isDialogShowAgain.asLiveData()
    fun setDialogShowAgain(answer: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setDialogShowAgain(answer)
    }

}