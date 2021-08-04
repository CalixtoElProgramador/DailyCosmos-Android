package com.listocalixto.dailycosmos.data.local

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import com.listocalixto.dailycosmos.data.model.toAPODList

class LocalAPODDataSource(private val apodDao: APODDao) {

    suspend fun getResults(): List<APOD> {
        return apodDao.getAllAPODs().toAPODList()
    }

    suspend fun saveAPOD(apodEntity: APODEntity) {
        apodDao.saveAPOD(apodEntity)
    }
}