package com.listocalixto.dailycosmos.data.local

import androidx.room.*
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity

@Dao
interface APODDao {

    @Query("SELECT * FROM apodentity ORDER BY date DESC")
    suspend fun getAllAPODs(): List<APODEntity>

    @Insert(onConflict= OnConflictStrategy.IGNORE)
    suspend fun saveAPOD(apod: APODEntity)

    @Update(entity = APODEntity::class)
    suspend fun updateFavorite(apod: APODEntity)

}