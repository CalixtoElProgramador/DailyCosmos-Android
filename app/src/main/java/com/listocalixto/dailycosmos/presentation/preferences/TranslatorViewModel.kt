package com.listocalixto.dailycosmos.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.data.datastore.TranslatorDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslatorViewModel @Inject constructor(private val repo: TranslatorDataStore) : ViewModel() {

    val readValue = repo.readValue.asLiveData()
    fun saveValue(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveValue(value)
    }

}