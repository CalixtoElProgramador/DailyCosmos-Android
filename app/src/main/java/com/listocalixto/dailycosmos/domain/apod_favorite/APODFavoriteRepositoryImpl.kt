package com.listocalixto.dailycosmos.domain.apod_favorite

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity
import com.listocalixto.dailycosmos.data.remote.apod_favorite.RemoteAPODFavoriteDataSource

class APODFavoriteRepositoryImpl(private val dataSource: RemoteAPODFavoriteDataSource) :
    APODFavoriteRepository {

    override suspend fun setAPODFavorite(apod: APOD) {
        dataSource.setAPODFavorite(apod)
    }

    override suspend fun getAPODFavorites(): List<APODFavoriteEntity> = dataSource.getAPODFavorites()
}