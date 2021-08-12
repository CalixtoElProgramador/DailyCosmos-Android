package com.listocalixto.dailycosmos.data.local.favorites

import com.listocalixto.dailycosmos.data.model.FavoriteEntity

class LocalFavoriteDataSource(private val dao: FavoriteDao) {

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