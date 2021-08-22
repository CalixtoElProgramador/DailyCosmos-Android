package com.listocalixto.dailycosmos.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter

class MainViewModel : ViewModel() {

    private val detailsArgs = MutableLiveData<DetailsArgs>()
    private val userHaveInternet = MutableLiveData<Boolean>()
    private val pictureArgs = MutableLiveData<PictureArgs>()
    private val dateToFind = MutableLiveData<String>()

    fun setArgsToDetails(args: DetailsArgs) { detailsArgs.value = args }
    fun getArgsToDetails(): LiveData<DetailsArgs> = detailsArgs

    fun setUserHaveInternet(answer: Boolean) { userHaveInternet.value = answer }
    fun isUserHaveInternet(): LiveData<Boolean> = userHaveInternet

    fun setArgsToPicture(args: PictureArgs) { pictureArgs.value = args }
    fun getArgsToPicture(): LiveData<PictureArgs> = pictureArgs

    fun setDateToFind(date: String) { dateToFind.value = date }
    fun getDateToFind(): LiveData<String> = dateToFind

}

data class DetailsArgs(
    val apod: APOD,
    val adapterExplore: ExploreAdapter?,
    val position: Int
)

data class PictureArgs(
    val hdurl: String,
    val url: String,
    val title: String
)