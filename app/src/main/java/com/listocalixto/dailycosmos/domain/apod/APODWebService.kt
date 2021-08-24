    package com.listocalixto.dailycosmos.domain.apod

import com.google.gson.GsonBuilder
import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.model.APOD
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface APODWebService {

    @GET("planetary/apod")
    suspend fun getResults(
        @Query("api_key") apiKey: String,
        @Query("start_date") starDate: String,
        @Query("end_date") endDate: String
    ): List<APOD>

    @GET("planetary/apod")
    suspend fun getRandomResults(
        @Query("api_key") apiKey: String,
        @Query("count") count: String
    ): List<APOD>

}
