package com.listocalixto.dailycosmos.data.remote.auth

import android.graphics.Bitmap
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.listocalixto.dailycosmos.data.model.User
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class AuthDataSource {

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
        return if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
            val credential = EmailAuthProvider.getCredential(email, password)
            val authResult = FirebaseAuth.getInstance().currentUser?.linkWithCredential(credential)?.await()

            authResult?.user?.uid?.let { uid ->
                val imageRef = FirebaseStorage.getInstance().reference.child("${uid}/profile_picture")
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val downloadUrl =
                    imageRef.putBytes(baos.toByteArray()).await().storage.downloadUrl.await().toString()
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .set(User(name, lastname, email, downloadUrl, uid)).await()
            }
            authResult?.user

        } else {
            val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            authResult.user?.uid?.let { uid ->
                val imageRef = FirebaseStorage.getInstance().reference.child("${uid}/profile_picture")
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val downloadUrl =
                    imageRef.putBytes(baos.toByteArray()).await().storage.downloadUrl.await().toString()
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .set(User(name, lastname, email, downloadUrl, uid)).await()
            }
            authResult.user
        }
    }

    suspend fun assignCredentialToGuest(email: String, password: String): AuthResult? {
        val credential = EmailAuthProvider.getCredential(email, password)
        val authResult = Firebase.auth.currentUser!!.linkWithCredential(credential).await()

        return authResult
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        val isRegistered =
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", email).get()
                .await()
        return isRegistered.isEmpty
    }

    suspend fun getCurrentUser(): User? {
        val user = FirebaseAuth.getInstance().currentUser
        val querySnapshot =
            FirebaseFirestore.getInstance().collection("users").document("${user?.uid}").get()
                .await()
        return querySnapshot.toObject(User::class.java)
    }
}