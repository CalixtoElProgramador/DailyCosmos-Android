package com.listocalixto.dailycosmos.domain.favorites

import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.FavoriteEntity

interface FavoritesRepo {

    suspend fun saveFavorite(apod: APOD)
    suspend fun deleteFavorite(apod: APOD)
    suspend fun getFavorites(): List<FavoriteEntity>

}