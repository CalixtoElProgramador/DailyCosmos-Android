package com.listocalixto.dailycosmos.presentation.apod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.domain.apod.APODRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class APODViewModel(private val repo: APODRepository) : ViewModel() {

    fun fetchFirstTimeResults(endDate: String, startDate: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getFirstTimeResults(endDate, startDate)
        }.onSuccess { apodList ->
            emit(Result.Success(apodList))
        }.onFailure {
            Result.Failure(Exception(it.message))
        }
    }

    fun fetchMoreResults(endDate: String, startDate: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getMoreResults(endDate, startDate)
        }.onSuccess { apodList ->
            emit(Result.Success(apodList))
        }.onFailure {
            Result.Failure(Exception(it.message))
        }
    }

    fun fetchRandomResults(count: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getRandomResults(count)
        }.onSuccess {
            emit(Result.Success(it))
        }.onFailure {
            emit(Result.Failure(Exception(it.message)))
        }
    }

    fun fetchCalendarResults(endDate: String, startDate: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getCalendarResults(endDate, startDate)
        }.onSuccess {
            emit(Result.Success(it))
        }.onFailure {
            emit(Result.Failure(Exception(it.message)))
        }
    }

    fun fetchSearchResults(searchQuery: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getSearchResults(searchQuery)
        }.onSuccess {
            emit(Result.Success(it))
        }.onFailure {
            emit(Result.Failure(Exception(it.message)))
        }
    }

    fun fetchDataFromDatabase() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getDataFromDatabase()
        }.onSuccess {
            emit(Result.Success(it))
        }.onFailure {
            emit(Result.Failure(Exception(it.message)))
        }
    }

    fun updateFavorite(apod: APOD, isFavorite: Int) {
        viewModelScope.launch {
            repo.updateFavorite(apod, isFavorite)
        }
    }
}

class APODViewModelFactory(private val repo: APODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(APODRepository::class.java).newInstance(repo)
    }
}