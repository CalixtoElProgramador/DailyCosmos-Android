package com.listocalixto.dailycosmos.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.listocalixto.dailycosmos.data.model.User

class SettingsViewModel : ViewModel() {

    private val userArgs = MutableLiveData<User?>()

    fun setUser(user: User?) {
        userArgs.value = user
    }

    fun getUser(): LiveData<User?> = userArgs

}