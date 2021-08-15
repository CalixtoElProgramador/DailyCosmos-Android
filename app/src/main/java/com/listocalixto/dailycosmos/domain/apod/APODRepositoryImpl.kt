package com.listocalixto.dailycosmos.domain.apod

import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.toAPODEntity
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource

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
            dataSourceFireStore.getRemoteFavorites().forEach { favorite ->
                dataSourceLocal.updateFavorite(favorite.toAPODEntity(1))
            }

            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }

    override suspend fun getRandomResults(count: String): List<APOD> =
        dataSourceRemote.getRandomResults(count)

    override suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD> {
        return dataSourceRemote.getCalendarResults(endDate, startDate)
    }

    override suspend fun getSearchResults(searchQuery: String): List<APOD> {
        return dataSourceLocal.getSearchResults(searchQuery)
    }

    override suspend fun updateFavorite(apod: APOD, isFavorite: Int) {
        dataSourceLocal.updateFavorite(apod.toAPODEntity(isFavorite))
    }

    //override fun getAPOD(date: String): Flow<APOD> = dataSourceLocal.getAPOD(date)
}