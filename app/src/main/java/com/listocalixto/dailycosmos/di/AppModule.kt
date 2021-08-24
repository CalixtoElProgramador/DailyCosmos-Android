package com.listocalixto.dailycosmos.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.domain.apod.APODWebService
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRoomInstance(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "apod_table").build()

    @Singleton
    @Provides
    fun provideAPODDao(db: AppDatabase) = db.apodDao()

    @Singleton
    @Provides
    fun provideFavoriteDao(db: AppDatabase) = db.favoriteDao()

    @Singleton
    @Provides
    fun provideRetrofitInstance(): Retrofit = Retrofit.Builder()
        .baseUrl(AppConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    @Singleton
    @Provides
    fun provideWebService(retrofit: Retrofit): APODWebService = retrofit.create(APODWebService::class.java)

}