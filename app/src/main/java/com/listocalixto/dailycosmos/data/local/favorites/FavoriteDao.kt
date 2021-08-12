package com.listocalixto.dailycosmos.data.local.favorites

import androidx.room.*
import com.listocalixto.dailycosmos.data.model.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favoriteentity ORDER BY date DESC")
    suspend fun getFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorite(favoriteEntity: FavoriteEntity)

    @Delete(entity = FavoriteEntity::class)
    suspend fun deleteFavorite(favoriteEntity: FavoriteEntity)

}