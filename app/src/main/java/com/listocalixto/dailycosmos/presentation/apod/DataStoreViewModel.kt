package com.listocalixto.dailycosmos.presentation.apod

import android.app.Application
import androidx.lifecycle.*
import com.listocalixto.dailycosmos.repository.apod.APODDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DataStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = APODDataStore(application)

    val readLastDateFromDataStore = repo.readLastDateFromDataStore.asLiveData()

    fun saveLastDateToDataStore(newStarDate: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveLastDateToDataStore(newStarDate)
    }

    val readSizeListFromDataStore = repo.readListSizeFromDataStore.asLiveData()

    fun saveNewSizeListToDataStore(size: Int) = viewModelScope.launch {
        Dispatchers.IO
        repo.saveNewListSizeToDataStore(size)
    }

}
