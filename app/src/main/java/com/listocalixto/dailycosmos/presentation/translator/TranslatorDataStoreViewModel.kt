package com.listocalixto.dailycosmos.presentation.translator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.repository.translator.TranslatorDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslatorDataStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TranslatorDataStore(application)

    val readValue = repo.readValue.asLiveData()
    fun saveValue(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.saveValue(value)
    }

}