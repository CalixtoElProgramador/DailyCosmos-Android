package com.listocalixto.dailycosmos.domain.apod

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import kotlinx.coroutines.flow.Flow

interface APODRepo {

    suspend fun getFirstTimeResults(endDate: String, startDate: String): List<APOD>
    suspend fun getMoreResults(endDate: String, startDate: String): List<APOD>
    suspend fun getRecentResults(endDate: String, startDate: String): List<APOD>
    suspend fun getRandomResults(count: String): List<APOD>
    suspend fun getSearchResults(searchQuery: String): List<APOD>
    suspend fun getDataFromDatabase(): List<APOD>
    suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD>
    suspend fun updateFavorite(apod: APOD, isFavorite: Int)

}