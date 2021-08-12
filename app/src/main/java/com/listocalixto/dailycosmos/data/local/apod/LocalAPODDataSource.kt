package com.listocalixto.dailycosmos.data.local.apod

import com.listocalixto.dailycosmos.data.model.*

class LocalAPODDataSource(private val apodDao: APODDao) {

    suspend fun getResults(): List<APOD> {
        return apodDao.getAPODs().toAPODList()
    }

    suspend fun getFavorites(): List<APODEntity> {
        return apodDao.getFavorites()
    }

    suspend fun saveAPOD(apodEntity: APODEntity) {
        apodDao.saveAPOD(apodEntity)
    }

    suspend fun updateFavorite(apodEntity: APODEntity) {
        apodDao.updateFavorite(apodEntity)
    }

    /*fun getAPOD(date: String): Flow<APOD> = apodDao.getAPODDistinctUntilChanged(date)*/

}