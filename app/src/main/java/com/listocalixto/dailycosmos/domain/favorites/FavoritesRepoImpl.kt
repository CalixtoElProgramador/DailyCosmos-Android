package com.listocalixto.dailycosmos.domain.favorites

import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import com.listocalixto.dailycosmos.data.model.toFavorite
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource

class FavoritesRepoImpl(
    private val dataSourceRemote: RemoteAPODFavoriteDataSource,
    private val dataSourceLocalFav: LocalFavoriteDataSource,
    private val dataSourceLocal: LocalAPODDataSource
) :
    FavoritesRepo {

    override suspend fun saveFavorite(apod: APOD) {
        dataSourceRemote.setRemoteFavorite(apod)
        dataSourceLocalFav.saveFavorite(apod.toFavorite(""))

    }

    override suspend fun deleteFavorite(apod: APOD) {
        dataSourceRemote.deleteRemoteFavorite(apod)
        dataSourceLocalFav.deleteFavorite(apod.toFavorite(""))
    }

    override suspend fun getFavorites(): List<FavoriteEntity> {
        return if (InternetCheck.isNetworkAvailable()) {
            dataSourceRemote.getRemoteFavorites().forEach { favoriteEntity ->
                dataSourceLocalFav.saveFavorite(favoriteEntity)
            }

            dataSourceLocal.getFavorites().forEach { apodEntity ->
                dataSourceLocalFav.saveFavorite(apodEntity.toFavorite(""))
            }

            dataSourceLocalFav.getFavorites()
        } else {
            dataSourceLocal.getFavorites().forEach { apodEntity ->
                dataSourceLocalFav.saveFavorite(apodEntity.toFavorite(""))
            }
            dataSourceLocalFav.getFavorites()
        }
    }
}

