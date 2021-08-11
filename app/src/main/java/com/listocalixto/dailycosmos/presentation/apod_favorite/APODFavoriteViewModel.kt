package com.listocalixto.dailycosmos.presentation.apod_favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.domain.apod_favorite.APODFavoriteRepository
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

class APODFavoriteViewModel(private val repo: APODFavoriteRepository) : ViewModel() {

    fun setAPODFavorite(apod: APOD) = liveData(Dispatchers.IO) {
        emit(Result.Loading())

        try {
            emit(Result.Success(repo.setAPODFavorite(apod)))

        } catch (e: Exception) {
            emit(Result.Failure(e))

        }
    }

    fun getAPODFavorites() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.getAPODFavorites()))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}

class APODFavoriteViewModelFactory(private val repo: APODFavoriteRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(APODFavoriteRepository::class.java).newInstance(repo)
    }
}