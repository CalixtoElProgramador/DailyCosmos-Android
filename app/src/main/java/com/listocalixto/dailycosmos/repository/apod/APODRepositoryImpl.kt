package com.listocalixto.dailycosmos.repository.apod

import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.toAPODEntity
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource

class APODRepositoryImpl(
    private val dataSourceRemote: RemoteAPODDataSource,
    private val dataSourceLocal: LocalAPODDataSource
) : APODRepository {

    override suspend fun getResults(endDate: String, startDate: String): List<APOD> {
        return if (InternetCheck.isNetworkAvailable()) {
            dataSourceRemote.getResults(endDate, startDate).asReversed().forEach { apod ->
                dataSourceLocal.saveAPOD(apod.toAPODEntity())
            }
            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }
}