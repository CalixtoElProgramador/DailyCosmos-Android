package com.listocalixto.dailycosmos.domain.auth

import android.graphics.Bitmap
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import javax.inject.Inject

class AuthRepoImpl @Inject constructor (private val dataSource: AuthDataSource) : AuthRepo {

    override suspend fun singIn(email: String, password: String): FirebaseUser? {
        return dataSource.signIn(email, password)
    }

    override suspend fun signUp(
        name: String,
        lastname: String,
        email: String,
        password: String,
        imageBitmap: Bitmap
    ): FirebaseUser? {
        return dataSource.signUp(name, lastname, email, password, imageBitmap)
    }

    override suspend fun assignCredentialToGuest(email: String, password: String): AuthResult? {
        return dataSource.assignCredentialToGuest(email, password)
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        return dataSource.isEmailRegistered(email)
    }

    override suspend fun getCurrentUser(): User? {
        return dataSource.getCurrentUser()
    }
}