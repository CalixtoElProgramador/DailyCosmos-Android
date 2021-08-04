package com.listocalixto.dailycosmos.presentation.apod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.repository.apod.APODRepository
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

class APODViewModel(private val repo: APODRepository) : ViewModel() {

    fun fetchAPODResults(endDate: String, startDate: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.getResults(endDate, startDate)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}

class APODViewModelFactory(private val repo: APODRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(APODRepository::class.java).newInstance(repo)
    }
}