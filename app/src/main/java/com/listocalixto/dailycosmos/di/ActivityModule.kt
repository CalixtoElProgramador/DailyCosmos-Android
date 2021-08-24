package com.listocalixto.dailycosmos.di

import com.listocalixto.dailycosmos.domain.apod.APODRepo
import com.listocalixto.dailycosmos.domain.apod.APODRepoImpl
import com.listocalixto.dailycosmos.domain.auth.AuthRepo
import com.listocalixto.dailycosmos.domain.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepo
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.DefineComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ActivityModule {

    @Binds
    abstract fun bindAPODRepoImpl(repoImpl: APODRepoImpl): APODRepo

    @Binds
    abstract fun bindAuthRepoImpl(repoImpl: AuthRepoImpl): AuthRepo

    @Binds
    abstract fun bindFavoritesRepoImpl(repoImpl: FavoritesRepoImpl): FavoritesRepo

}