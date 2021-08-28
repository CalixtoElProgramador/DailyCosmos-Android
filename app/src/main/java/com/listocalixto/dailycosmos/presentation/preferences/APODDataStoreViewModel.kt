package com.listocalixto.dailycosmos.presentation.preferences

import android.app.Application
import androidx.lifecycle.*
import com.listocalixto.dailycosmos.data.datastore.APODDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class APODDataStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = APODDataStore(application)

    val readLastDateFromDataStore = repo.readLastDateFromDataStore.asLiveData()
    fun saveLastDateToDataStore(newStarDate: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveLastDateToDataStore(newStarDate)
    }

    val readReferenceDate = repo.readReferenceDate.asLiveData()
    fun saveReferenceDate(reference: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveReferenceDate(reference)
    }

}
