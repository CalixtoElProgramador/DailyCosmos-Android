package com.listocalixto.dailycosmos.domain.apod

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import kotlinx.coroutines.flow.Flow

interface APODRepository {

    suspend fun getResults(endDate: String, startDate: String): List<APOD>
    suspend fun getRandomResults(count: String): List<APOD>
    suspend fun getSearchResults(searchQuery: String): List<APOD>
    suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD>

    suspend fun updateFavorite(apod: APOD, isFavorite: Int)
    //fun getAPOD(date: String): Flow<APOD>

}