package com.listocalixto.dailycosmos.data.remote.apod_favorite

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.APODEntity
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity
import kotlinx.coroutines.tasks.await

class RemoteAPODFavoriteDataSource {

    suspend fun setAPODFavorite(apod: APOD) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("favorites").add(
                APODFavoriteEntity(
                    apod.date,
                    apod.copyright,
                    apod.explanation,
                    apod.hdurl,
                    apod.media_type,
                    apod.title,
                    apod.url,
                    user.uid
                )
            ).await()
        }
    }

    suspend fun getAPODFavorites(): Result<List<APODFavoriteEntity>> {
        val user = FirebaseAuth.getInstance().currentUser
        val favoritesList = mutableListOf<APODFavoriteEntity>()
        val querySnapshot =
            FirebaseFirestore.getInstance().collection("favorites").whereEqualTo("uid", user?.uid)
                .get().await()
        for (favorite in querySnapshot.documents) {
            favorite.toObject(APODFavoriteEntity::class.java)?.let {
                favoritesList.add(it)
            }
        }
        return Result.Success(favoritesList)
    }
}