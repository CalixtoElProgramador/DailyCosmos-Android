package com.listocalixto.dailycosmos.data.remote.favorites

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import com.listocalixto.dailycosmos.data.model.toFavorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RemoteAPODFavoriteDataSource {

    suspend fun setRemoteFavorite(apod: APOD) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("favorites")
                .document("${apod.date} ${user.uid}").set(apod.toFavorite(user.uid)).await()
        }
    }

    suspend fun deleteRemoteFavorite(apod: APOD) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("favorites")
                .document("${apod.date} ${user.uid}").delete().await()
        }
    }

    suspend fun getRemoteFavorites(): List<FavoriteEntity> {
        val user = FirebaseAuth.getInstance().currentUser
        val favoritesList = mutableListOf<FavoriteEntity>()

        withContext(Dispatchers.IO) {
            val querySnapshot =
                FirebaseFirestore.getInstance().collection("favorites")
                    .whereEqualTo("uid", user?.uid)
                    .get().await()
            for (favorite in querySnapshot.documents) {
                favorite.toObject(FavoriteEntity::class.java)?.let {
                    favoritesList.add(it)
                }
            }
        }

        return favoritesList
    }
}