package com.listocalixto.dailycosmos.domain.apod

import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.core.InternetCheck
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.toAPODEntity
import com.listocalixto.dailycosmos.data.model.toFavorite
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource

class APODRepositoryImpl(
    private val dataSourceRemote: RemoteAPODDataSource,
    private val dataSourceLocal: LocalAPODDataSource,
    private val dataSourceFireStore: RemoteAPODFavoriteDataSource,
    private val dataSourceLocalFavorites: LocalFavoriteDataSource
) : APODRepository {

    override suspend fun getFirstTimeResults(endDate: String, startDate: String): List<APOD> {
        return if (InternetCheck.isNetworkAvailable()) {
            dataSourceRemote.getResults(endDate, startDate).forEach { apod ->
                dataSourceLocal.saveAPOD(apod.toAPODEntity(0))
            }
            dataSourceFireStore.getRemoteFavorites().forEach { favoriteEntity ->
                dataSourceLocalFavorites.saveFavorite(favoriteEntity)
            }

            dataSourceLocalFavorites.getFavorites().forEach { favoriteEntity ->
                dataSourceLocal.updateFavorite(favoriteEntity.toAPODEntity(1))
            }

            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }

    override suspend fun getMoreResults(endDate: String, startDate: String): List<APOD> {
        return if (InternetCheck.isNetworkAvailable()) {
            dataSourceRemote.getResults(endDate, startDate).forEach { apod ->
                dataSourceLocal.saveAPOD(apod.toAPODEntity(0))
            }
            dataSourceLocalFavorites.getFavorites().forEach { favoriteEntity ->
                dataSourceLocal.updateFavorite(favoriteEntity.toAPODEntity(1))
            }
            dataSourceLocal.getResults()
        } else {
            dataSourceLocal.getResults()
        }
    }

    override suspend fun getRandomResults(count: String): List<APOD> {
        val randomList = dataSourceRemote.getRandomResults(count)
        randomList.forEachIndexed { index, apod ->
            if (dataSourceLocalFavorites.getFavorites().contains(apod.toFavorite(FirebaseAuth.getInstance().uid))) {
                randomList[index].is_favorite = 1
            }
        }
        return randomList
    }

    override suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD> {
        val calendarList = dataSourceRemote.getCalendarResults(endDate, startDate)
        calendarList.forEachIndexed { index, apod ->
            if (dataSourceLocalFavorites.getFavorites()
                    .contains(apod.toFavorite(FirebaseAuth.getInstance().uid))
            ) {
                calendarList[index].is_favorite = 1
            }
        }
        return calendarList
    }

    override suspend fun getSearchResults(searchQuery: String): List<APOD> {
        return dataSourceLocal.getSearchResults(searchQuery)
    }

    override suspend fun getDataFromDatabase(): List<APOD> {
        return dataSourceLocal.getDataFromDatabase()
    }

    override suspend fun updateFavorite(apod: APOD, isFavorite: Int) {
        dataSourceLocal.updateFavorite(apod.toAPODEntity(isFavorite))
    }

    //override fun getAPOD(date: String): Flow<APOD> = dataSourceLocal.getAPOD(date)
}