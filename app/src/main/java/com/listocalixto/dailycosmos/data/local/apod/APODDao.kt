package com.listocalixto.dailycosmos.data.local.apod

import androidx.room.*
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface APODDao {

    @Query("SELECT * FROM apodentity ORDER BY date DESC")
    suspend fun getResults(): List<APODEntity>

    @Query("SELECT * FROM apodentity WHERE is_favorite = 1")
    suspend fun getFavorites(): List<APODEntity>

    @Query("SELECT * FROM apodentity WHERE title LIKE :searchQuery OR date LIKE :searchQuery ORDER BY date DESC")
    suspend fun getSearchResults(searchQuery: String): List<APODEntity>

    @Query("SELECT * FROM apodentity ORDER BY date DESC")
    suspend fun getDataFromDatabase(): List<APODEntity>

    //fun getAllAPODsDistinctUntilChanged() = getAllAPODs().distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveAPOD(apodEntity: APODEntity)

    @Update(entity = APODEntity::class)
    suspend fun updateFavorite(apodEntity: APODEntity)

    /*@Query("SELECT * FROM apodentity WHERE date = :date")
    fun getAPOD(date: String): Flow<APOD>
    fun getAPODDistinctUntilChanged(date:String) = getAPOD(date).distinctUntilChanged()*/
}