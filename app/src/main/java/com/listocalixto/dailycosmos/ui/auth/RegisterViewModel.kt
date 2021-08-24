package com.listocalixto.dailycosmos.ui.auth

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    private val personData = MutableLiveData<Person>()
    private val passwordData = MutableLiveData<Password>()
    private val bitmapData = MutableLiveData<Bitmap>()

    fun setPerson(person: Person) { personData.value = person }
    fun getPerson(): LiveData<Person> = personData

    fun setPassword(password: Password) { passwordData.value = password }
    fun getPassword(): LiveData<Password> = passwordData

    fun setBitmap(bitmap: Bitmap) { bitmapData.value = bitmap }
    fun getBitmap(): LiveData<Bitmap> = bitmapData

}

data class Person(val name: String, val lastname: String, val email: String)

data class Password(val password: String, val passwordConfirm: String)