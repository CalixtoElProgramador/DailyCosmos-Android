package com.listocalixto.dailycosmos.repository.apod

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

}

object RetrofitClient {

    val webservice by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(APODWebService::class.java)
    }

}