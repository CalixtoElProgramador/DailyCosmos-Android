package com.listocalixto.dailycosmos.ui.auth.register

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    private val personData = MutableLiveData<Person>()

    fun setPerson(person: Person) {
        personData.value = person
    }

    fun getPerson(): LiveData<Person> = personData

    private val passwordData = MutableLiveData<Password>()

    fun setPassword(password: Password) {
        passwordData.value = password
    }

    fun getPassword(): LiveData<Password> = passwordData

    private val bitmapData = MutableLiveData<Bitmap>()

    fun setBitmap(bitmap: Bitmap) {
        bitmapData.value = bitmap
    }

    fun getBitmap(): LiveData<Bitmap> = bitmapData

}

data class Person(val name: String, val lastname: String, val email: String)

data class Password(val password: String, val passwordConfirm: String)