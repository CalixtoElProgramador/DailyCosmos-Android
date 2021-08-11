package com.listocalixto.dailycosmos.domain.apod

import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.toAPODEntity
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.data.remote.apod_favorite.RemoteAPODFavoriteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class APODRepositoryImpl(
    private val dataSourceRemote: RemoteAPODDataSource,
    private val dataSourceLocal: LocalAPODDataSource,
    private val dataSourceFireStore: RemoteAPODFavoriteDataSource
) : APODRepository {

    override suspend fun getResults(endDate: String, startDate: String): List<APOD> {
        return if (InternetCheck.isNetworkAvailable()) {
            dataSourceRemote.getResults(endDate, startDate).forEach { apod ->
                dataSourceLocal.saveAPOD(apod.toAPODEntity(0))
            }
            dataSourceFireStore.getAPODFavorites().forEach { favorite ->
                dataSourceLocal.updateFavorite(favorite.toAPODEntity(1))
            }
            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }

    override suspend fun updateFavorite(apod: APOD, isFavorite: Int) {
        dataSourceLocal.updateFavorite(apod.toAPODEntity(isFavorite))
    }

    //override fun getAPOD(date: String): Flow<APOD> = dataSourceLocal.getAPOD(date)
}