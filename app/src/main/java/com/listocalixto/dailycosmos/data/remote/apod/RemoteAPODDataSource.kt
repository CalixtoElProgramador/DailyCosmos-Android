package com.listocalixto.dailycosmos.data.remote.apod

import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.domain.apod.APODWebService
import javax.inject.Inject

class RemoteAPODDataSource @Inject constructor (private val webService: APODWebService) {

    suspend fun getResults(endDate: String, startDate: String): List<APOD> = webService.getResults(
        AppConstants.API_KEY,
        startDate,
        endDate
    )

    suspend fun getRandomResults(count: String): List<APOD> = webService.getRandomResults(
        AppConstants.API_KEY,
        count
    )

    suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD> =
        webService.getResults(
            AppConstants.API_KEY,
            startDate,
            endDate
        )

}