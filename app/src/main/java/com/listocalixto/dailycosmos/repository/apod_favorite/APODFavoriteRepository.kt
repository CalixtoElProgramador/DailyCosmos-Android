package com.listocalixto.dailycosmos.repository.apod_favorite

import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity

interface APODFavoriteRepository {

    suspend fun setAPODFavorite(apod: APOD)

    suspend fun getAPODFavorites(): Result<List<APODFavoriteEntity>>

}