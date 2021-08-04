package com.listocalixto.dailycosmos.data.remote.auth

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.listocalixto.dailycosmos.data.model.User
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class UserDataSource {

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        val authResult =
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
        return authResult.user
    }

    suspend fun signUp(
        name: String,
        lastname: String,
        email: String,
        password: String,
        imageBitmap: Bitmap
    ): FirebaseUser? {
        val authResult =
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
        authResult.user?.uid?.let { uid ->
            val imageRef = FirebaseStorage.getInstance().reference.child("${uid}/profile_picture")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val downloadUrl =
                imageRef.putBytes(baos.toByteArray()).await().storage.downloadUrl.await().toString()
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(User(name, lastname, email, downloadUrl, uid)).await()
        }
        return authResult.user
    }

    suspend fun isEmailRegister(email: String): Boolean {
        val isRegister =
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", email).get()
                .await()
        return isRegister.isEmpty
    }

}