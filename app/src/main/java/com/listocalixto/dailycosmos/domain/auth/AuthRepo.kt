package com.listocalixto.dailycosmos.domain.auth

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseUser
import com.listocalixto.dailycosmos.data.model.User

interface AuthRepo {

    suspend fun singIn(email: String, password: String): FirebaseUser?
    suspend fun signUp(name: String, lastname: String, email: String, password: String, imageBitmap: Bitmap): FirebaseUser?
    suspend fun isEmailRegistered(email: String): Boolean
    suspend fun getCurrentUser(): User?

}