package com.listocalixto.dailycosmos.presentation.preferences

import androidx.lifecycle.*
import com.listocalixto.dailycosmos.data.datastore.APODDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class APODDataStoreViewModel @Inject constructor(private val repo: APODDataStore) : ViewModel() {

    val readLastDateFromDataStore = repo.readLastDateFromDataStore.asLiveData()
    fun saveLastDateToDataStore(newStarDate: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveLastDateToDataStore(newStarDate)
    }

    val readReferenceDate = repo.readReferenceDate.asLiveData()
    fun saveReferenceDate(reference: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveReferenceDate(reference)
    }

}
