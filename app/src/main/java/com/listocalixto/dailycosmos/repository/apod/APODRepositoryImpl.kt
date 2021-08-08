package com.listocalixto.dailycosmos.repository.apod

import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import com.listocalixto.dailycosmos.data.model.toAPODEntity
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.data.remote.apod_favorite.RemoteAPODFavoriteDataSource

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

            dataSourceFireStore.getAPODFavorites().forEach {favorite ->
                dataSourceLocal.updateAPOD(APODEntity(
                    favorite.date,
                    favorite.copyright,
                    favorite.explanation,
                    favorite.hdurl,
                    favorite.media_type,
                    favorite.title,
                    favorite.url,
                    1
                ))
            }

            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }
}