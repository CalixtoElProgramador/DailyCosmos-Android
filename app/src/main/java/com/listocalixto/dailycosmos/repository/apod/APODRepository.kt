package com.listocalixto.dailycosmos.repository.apod

import com.listocalixto.dailycosmos.data.model.APOD

interface APODRepository {

    suspend fun getResults(endDate: String, startDate: String): List<APOD>

}