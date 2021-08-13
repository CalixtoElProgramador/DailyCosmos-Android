package com.listocalixto.dailycosmos.data.remote.apod

import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.domain.apod.APODWebService

class RemoteAPODDataSource(private val webService: APODWebService) {

    suspend fun getResults(endDate: String, startDate: String): List<APOD> = webService.getResults(
        AppConstants.API_KEY,
        startDate,
        endDate,
        "true"
    )

    suspend fun getRandomResults(count: String): List<APOD> = webService.getRandomResults(
        AppConstants.API_KEY,
        count,
        "true"
    )

    suspend fun getCalendarResults(endDate: String, startDate: String): List<APOD> =
        webService.getResults(
            AppConstants.API_KEY,
            startDate,
            endDate,
            "true"
        )

}