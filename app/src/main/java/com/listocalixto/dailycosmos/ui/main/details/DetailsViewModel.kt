package com.listocalixto.dailycosmos.ui.main.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetailsViewModel : ViewModel() {

    private val isFavorite = MutableLiveData<Int>()

    fun updateFavorite(value: Int?) {
        isFavorite.value = value
    }

    fun getValue(): LiveData<Int> = isFavorite

}