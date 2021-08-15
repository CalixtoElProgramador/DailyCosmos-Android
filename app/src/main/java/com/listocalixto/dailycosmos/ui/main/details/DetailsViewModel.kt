package com.listocalixto.dailycosmos.ui.main.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter
import com.listocalixto.dailycosmos.ui.main.favorites.adapter.FavoritesAdapter
import java.text.FieldPosition

class DetailsViewModel : ViewModel() {

    private val isFavorite = MutableLiveData<Int?>()
    private val detailsArgs = MutableLiveData<DetailsArgs>()

    fun setFavValue(value: Int?) {
        isFavorite.value = value
    }

    fun getFavValue(): LiveData<Int?> = isFavorite

    fun setArgs(args: DetailsArgs) {
        detailsArgs.value = args
    }

    fun getArgs(): LiveData<DetailsArgs> = detailsArgs

}

data class DetailsArgs(
    val apod: APOD,
    val adapterExplore: ExploreAdapter?,
    val position: Int
)