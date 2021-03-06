package com.listocalixto.dailycosmos.data.local.apod

import com.listocalixto.dailycosmos.data.model.*
import javax.inject.Inject

class LocalAPODDataSource @Inject constructor (private val apodDao: APODDao) {

    suspend fun getResults(): List<APOD> {
        return apodDao.getResults().toAPODList()
    }

    suspend fun getFavorites(): List<APODEntity> {
        return apodDao.getFavorites()
    }

    suspend fun getSearchResults(searchQuery: String): List<APOD> {
        return apodDao.getSearchResults(searchQuery).toAPODList()
    }

    suspend fun getDataFromDatabase(): List<APOD> {
        return apodDao.getResults().toAPODList()
    }

    suspend fun saveAPOD(apodEntity: APODEntity) {
        apodDao.saveAPOD(apodEntity)
    }

    suspend fun updateFavorite(apodEntity: APODEntity) {
        apodDao.updateFavorite(apodEntity)
    }

}