package com.listocalixto.dailycosmos.presentation.auth

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.listocalixto.dailycosmos.domain.auth.AuthRepo
import kotlinx.coroutines.Dispatchers
import com.listocalixto.dailycosmos.core.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (private val repo: AuthRepo) : ViewModel() {

    fun signIn(email: String, password: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.singIn(email, password)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

    fun signUp(
        name: String,
        lastname: String,
        email: String,
        password: String,
        imageBitmap: Bitmap
    ) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.signUp(name, lastname, email, password, imageBitmap)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

    fun isEmailRegister(email: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.isEmailRegistered(email)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

    fun getCurrentUser() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.getCurrentUser()))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

}
/*

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(private val repo: AuthRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}*/
