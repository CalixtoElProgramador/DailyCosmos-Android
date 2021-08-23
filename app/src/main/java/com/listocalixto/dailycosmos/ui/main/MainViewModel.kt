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
    private val delta = MutableLiveData<Int>()
    private val dateRange = MutableLiveData<DateRange>()
    private val firstTimeOpen = MutableLiveData<Boolean>()
    private val listAPOD = MutableLiveData<APODList>()
    private val disableLoadMoreResults = MutableLiveData<LoadMoreResults>()
    private val expandedExploreAppBar = MutableLiveData<Boolean>()
    private val dateRangePicker = MutableLiveData<DateRangePicker>()
    private val apodTranslated =  MutableLiveData<APODTranslated?>()

    fun setArgsToDetails(args: DetailsArgs) { detailsArgs.value = args }
    fun getArgsToDetails(): LiveData<DetailsArgs> = detailsArgs

    fun setUserHaveInternet(answer: Boolean) { userHaveInternet.value = answer }
    fun isUserHaveInternet(): LiveData<Boolean> = userHaveInternet

    fun setArgsToPicture(args: PictureArgs) { pictureArgs.value = args }
    fun getArgsToPicture(): LiveData<PictureArgs> = pictureArgs

    fun setDelta(value: Int) { delta.value = value}
    fun getDelta(): LiveData<Int> = delta

    fun setDateRange(range: DateRange) { dateRange.value = range }
    fun getDateRange(): LiveData<DateRange> = dateRange

    fun setFirstTimeOpen(answer: Boolean) { firstTimeOpen.value = answer }
    fun isFirstTimeOpen(): LiveData<Boolean> = firstTimeOpen

    fun setAPODList(results: APODList) { listAPOD.value = results }
    fun getAPODList(): LiveData<APODList> = listAPOD

    fun setDisableLoadMoreResults(answers: LoadMoreResults) { disableLoadMoreResults.value = answers }
    fun isDisableLoadMoreResults(): LiveData<LoadMoreResults> = disableLoadMoreResults

    fun setExpandedExploreAppBar(answer: Boolean) { expandedExploreAppBar.value = answer }
    fun isExpandedExploreAppBar(): LiveData<Boolean> = expandedExploreAppBar

    fun setDateRangePicker(range: DateRangePicker) { dateRangePicker.value = range }
    fun getDateRangePicker(): LiveData<DateRangePicker> = dateRangePicker

    fun setAPODTranslated(translation: APODTranslated?) { apodTranslated.value = translation }
    fun getAPODTranslated(): LiveData<APODTranslated?> = apodTranslated

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

data class DateRange(
    val endDate: String,
    val startDate: String
)

data class APODList(
    val results: List<APOD>
)

data class LoadMoreResults(
    val isSearchResults: Boolean,
    val isLoading: Boolean
)

data class DateRangePicker(
    val firstDate: Long,
    val secondDate: Long
)

data class APODTranslated(
    val title: String,
    val explanation: String
)