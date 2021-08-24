package com.listocalixto.dailycosmos.data.local.favorites

import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import javax.inject.Inject

class LocalFavoriteDataSource @Inject constructor (private val dao: FavoriteDao) {

    suspend fun getFavorites(): List<FavoriteEntity> {
        return dao.getFavorites()
    }

    suspend fun saveFavorite(favorite: FavoriteEntity) {
        dao.saveFavorite(favorite)
    }

    suspend fun deleteFavorite(favorite: FavoriteEntity) {
        dao.deleteFavorite(favorite)
    }

}