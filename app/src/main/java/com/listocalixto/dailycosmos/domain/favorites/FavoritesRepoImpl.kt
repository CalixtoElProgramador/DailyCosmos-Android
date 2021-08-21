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
    private val dataSourceLocalFav: LocalFavoriteDataSource
) :
    FavoritesRepo {

    override suspend fun saveFavorite(apod: APOD) {
        dataSourceLocalFav.saveFavorite(apod.toFavorite(FirebaseAuth.getInstance().uid))
        dataSourceRemote.setRemoteFavorite(apod)

    }

    override suspend fun deleteFavorite(apod: APOD) {
        dataSourceLocalFav.deleteFavorite(apod.toFavorite(FirebaseAuth.getInstance().uid))
        dataSourceRemote.deleteRemoteFavorite(apod)
    }

    override suspend fun getFavorites(): List<FavoriteEntity> {
        return dataSourceLocalFav.getFavorites()
    }
}

