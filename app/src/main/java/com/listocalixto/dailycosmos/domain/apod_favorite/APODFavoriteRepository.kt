package com.listocalixto.dailycosmos.domain.apod_favorite

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity

interface APODFavoriteRepository {

    suspend fun setAPODFavorite(apod: APOD)
    suspend fun deleteFavorite(apod: APOD)
    suspend fun getAPODFavorites(): List<APODFavoriteEntity>

}