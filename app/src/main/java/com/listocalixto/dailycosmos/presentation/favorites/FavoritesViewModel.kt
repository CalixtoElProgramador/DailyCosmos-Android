package com.listocalixto.dailycosmos.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class APODFavoriteViewModel @Inject constructor (private val repo: FavoritesRepo) : ViewModel() {

    fun setAPODFavorite(apod: APOD) {
        viewModelScope.launch {
            repo.saveFavorite(apod)
        }
    }

    fun deleteFavorite(apod: APOD) {
        viewModelScope.launch {
            repo.deleteFavorite(apod)
        }
    }

    fun getAPODFavorites() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.getFavorites()))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}