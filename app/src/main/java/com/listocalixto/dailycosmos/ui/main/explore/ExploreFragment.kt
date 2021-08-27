package com.listocalixto.dailycosmos.ui.main.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.listocalixto.dailycosmos.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.databinding.FragmentExplorerBinding
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.preferences.APODDataStoreViewModel
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter
import com.listocalixto.dailycosmos.core.Result
import androidx.lifecycle.Observer
import com.google.android.material.color.MaterialColors
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.main.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explorer), ExploreAdapter.OnAPODClickListener {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val viewModelShared by activityViewModels<MainViewModel>()
    private val dataStore by activityViewModels<APODDataStoreViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val viewModel by activityViewModels<APODViewModel>()

    private var firstDatePicker = MaterialDatePicker.thisMonthInUtcMilliseconds()
    private var secondDatePicker = MaterialDatePicker.todayInUtcMilliseconds()
    private var isFirstSearch = -1
    private var isLoading = false
    private var isSearchResults = false
    private var isExpandedAppBar = true
    private var startDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DATE, -10)
    }

    private var referenceDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DATE, -10)
    }

    private lateinit var binding: FragmentExplorerBinding
    private lateinit var adapter: ExploreAdapter
    private lateinit var layoutManager: StaggeredGridLayoutManager
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var exploreList: List<APOD>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bottomNavigation = activity?.findViewById(R.id.bottom_navigation)!!

        viewModelShared.getAPODList().value?.let { list ->
            adapter = ExploreAdapter(list.results, this@ExploreFragment)
            exploreList = list.results
        }
        viewModelShared.isDisableLoadMoreResults().value?.let { answer ->
            isSearchResults = answer.isSearchResults
            isLoading = answer.isLoading
        }
        viewModelShared.isExpandedExploreAppBar().value?.let { answer ->
            isExpandedAppBar = answer
        }
        viewModelShared.getDateRangePicker().value?.let { range ->
            firstDatePicker = range.firstDate
            secondDatePicker = range.secondDate
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavigation.isInitialized) {
            showBottomNavView(bottomNavigation)
        }
        viewModelShared.setAPODTranslated(null)
    }

    private fun showBottomNavView(bottomNavigation: BottomNavigationView) {
        if (!bottomNavigation.isVisible) {
            bottomNavigation.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.slide_in_bottom
                )
                visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExplorerBinding.bind(view)
        if (!isExpandedAppBar) {
            binding.appBarExplorer.setExpanded(isExpandedAppBar)
        }
        configRecyclerView()
        isAdapterInit()
        readFromDataStore()
        loadMoreResults()

        bottomNavigation.setOnItemReselectedListener { item -> smoothScrollToStart(item) }
        binding.inputLayoutSearch.setEndIconOnClickListener { setHelperText(); validateQuery() }
        binding.topAppBar.setOnMenuItemClickListener { onMenuItemClick(it) }
    }

    private fun onMenuItemClick(it: MenuItem) = when (it.itemId) {
        R.id.settingsActivity -> {
            navigateToActivitySettings()
        }
        R.id.calendar -> {
            getCalendarResults()
        }
        R.id.random -> {
            getRandomResults()
        }
        else -> {
            showSnackbarMessage(getString(R.string.available_to_future_versions))
            true
        }
    }

    private fun getCalendarResults(): Boolean {
        openDatePicker()
        return true
    }

    private fun navigateToActivitySettings(): Boolean {
        findNavController().navigate(R.id.action_exploreFragment_to_settingsActivity)
        return true
    }

    private fun validateQuery() {
        val query = binding.inputSearch.text.toString()
        if (query.isEmpty()) {
            isDisableLoadMoreResults(false)
            getAllFromDatabase()

        } else {
            searchDatabase(query)
        }
    }

    private fun getAllFromDatabase() {
        viewModel.fetchDataFromDatabase().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    //Loading
                }
                is Result.Success -> {
                    exploreList = it.data
                    onSuccessResults(it)
                    viewModelShared.setExpandedExploreAppBar(false)
                }
                is Result.Failure -> {
                    //Failure
                }
            }
        })
    }

    private fun setHelperText() {
        when (isFirstSearch) {
            0 -> {
                binding.inputLayoutSearch.helperText =
                    getString(R.string.helper_text_search_view_02)
                dataStoreUtils.saveValueSearch(1)
            }
            1 -> {
                binding.inputLayoutSearch.helperText = ""
            }
        }
    }

    private fun smoothScrollToStart(item: MenuItem) {
        when (item.itemId) {
            R.id.exploreFragment -> {
                binding.rvApod.smoothScrollToPosition(0)
            }
        }
    }

    private fun setViewsInLoading() {
        binding.pbRvAPOD.visibility = View.VISIBLE
        binding.rvApod.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
    }

    private fun openDatePicker() {
        val constraintsBuilder = buildConstraint()
        val dateRangePicker = configDatePicker(constraintsBuilder)
        dateRangePicker.show(activity?.supportFragmentManager!!, "ExploreFragment")
        eventsDatePicker(dateRangePicker)

    }

    private fun configDatePicker(constraintsBuilder: CalendarConstraints.Builder): MaterialDatePicker<Pair<Long, Long>> {
        return MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.data_picker_title))
            .setSelection(
                Pair(firstDatePicker, secondDatePicker)
            ).setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.ThemeOverlay_DailyCosmos_DatePicker)
            .build()
    }

    private fun buildConstraint(): CalendarConstraints.Builder {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        calendar[Calendar.MONTH] = Calendar.JUNE
        calendar[Calendar.YEAR] = 1995
        calendar[Calendar.DATE] = 16
        val june1995 = calendar.timeInMillis

        return CalendarConstraints.Builder()
            .setStart(june1995)
            .setEnd(today)
    }

    @SuppressLint("ShowToast", "ResourceAsColor")
    private fun eventsDatePicker(dateRangePicker: MaterialDatePicker<Pair<Long, Long>>) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = today
            this[Calendar.DATE] = this[Calendar.DATE] + 1
        }
        val june161995 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            this[Calendar.DATE] = 16
            this[Calendar.MONTH] = Calendar.JUNE
            this[Calendar.YEAR] = 1995
        }

        dateRangePicker.addOnPositiveButtonClickListener {
            val firstDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val secondDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            firstDay.timeInMillis = it.first
            firstDay[Calendar.DATE] = firstDay[Calendar.DATE] + 1
            secondDay.timeInMillis = it.second
            secondDay[Calendar.DATE] = secondDay[Calendar.DATE] + 1

            val firstDayInMillis = firstDay.timeInMillis
            val secondDayInMillis = secondDay.timeInMillis
            val todayInMillis = todayCalendar.timeInMillis

            when {
                firstDayInMillis > todayInMillis || secondDayInMillis > todayInMillis -> {
                    showErrorSnackbarMessage(getString(R.string.error_request_future_dates_today))
                }
                firstDayInMillis < june161995.timeInMillis || secondDayInMillis < june161995.timeInMillis -> {
                    showErrorSnackbarMessage(getString(R.string.error_request_before_dates_june_16_1995))
                }

                secondDayInMillis - firstDayInMillis > AppConstants.MILLISECONDS_IN_A_MONTH -> {
                    showErrorSnackbarMessage(getString(R.string.error_request_more_31_days))
                }
                else -> {
                    getCalendarResults(sdf.format(firstDay.time), sdf.format(secondDay.time))
                    firstDatePicker = dateRangePicker.selection?.first!!
                    secondDatePicker = dateRangePicker.selection?.second!!
                    viewModelShared.setDateRangePicker(DateRangePicker(firstDatePicker, secondDatePicker))
                }
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun showErrorSnackbarMessage(message: String) {
        val colorError = MaterialColors.getColor(requireView(), R.attr.colorError)
        Snackbar.make(binding.topAppBar, message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottomNavigation)
            .setBackgroundTint(colorError)
            .show()
    }

    @SuppressLint("ShowToast")
    private fun showSnackbarMessage(message: String) {
        val colorError = MaterialColors.getColor(requireView(), R.attr.colorError)
        Snackbar.make(binding.topAppBar, message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottomNavigation)
            .show()
    }

    private fun getCalendarResults(start: String, end: String) {
        isDisableLoadMoreResults(true)
        viewModel.fetchCalendarResults(end, start).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    isSearchMethodsEnabled(false)
                    setViewsInLoading()
                }
                is Result.Success -> {
                    isSearchMethodsEnabled(true)
                    if (it.data.isEmpty()) {
                        showErrorMessage(
                            R.drawable.ic_help,
                            getString(R.string.how_strange),
                            getString(R.string.try_again_if_your_request_arrived_with_an_empty_response)
                        )
                        return@Observer
                    }
                    onSuccessResults(it)
                }
                is Result.Failure -> {
                    isSearchMethodsEnabled(true)
                    showErrorMessage(
                        R.drawable.ic_error_outline,
                        getString(R.string.something_went_wrong),
                        getString(R.string.verify_your_connection_internet_or_nasa_server_is_down)
                    )
                }
            }
        })
    }

    private fun setDataInRecyclerView(result: Result.Success<List<APOD>>) {
        adapter = ExploreAdapter(result.data, this@ExploreFragment)
        viewModelShared.setAPODList(APODList(result.data))
        binding.rvApod.adapter = adapter
    }

    private fun getRandomResults(): Boolean {
        isDisableLoadMoreResults(true)
        viewModel.fetchRandomResults("10").observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    isSearchMethodsEnabled(false)
                    setViewsInLoading()
                }
                is Result.Success -> {
                    isSearchMethodsEnabled(true)
                    if (it.data.isEmpty()) {
                        showErrorMessage(
                            R.drawable.ic_help,
                            getString(R.string.how_strange),
                            getString(R.string.try_again_if_your_request_arrived_with_an_empty_response)
                        )
                        return@Observer
                    }
                    onSuccessResults(it)
                }
                is Result.Failure -> {
                    isSearchMethodsEnabled(true)
                    showErrorMessage(
                        R.drawable.ic_error_outline,
                        getString(R.string.something_went_wrong),
                        getString(R.string.verify_your_connection_internet_or_nasa_server_is_down)
                    )
                }
            }
        })

        return true
    }

    private fun isSearchMethodsEnabled(answer: Boolean) {
        binding.inputLayoutSearch.isEnabled = answer
        binding.topAppBar.menu.findItem(R.id.random).isEnabled = answer
        binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = answer
    }

    private fun showErrorMessage(imageResource: Int, errorTitle: String, errorSubtitle: String) {
        binding.pbRvAPOD.visibility = View.GONE
        binding.rvApod.visibility = View.GONE
        binding.layoutNoResults.visibility = View.VISIBLE
        binding.imageError.setImageResource(imageResource)
        binding.textErrorTitle.text = errorTitle
        binding.textErrorSubtitle.text = errorSubtitle

    }

    private fun onSuccessResults(it: Result.Success<List<APOD>>) {
        setViewsInSuccess()
        setDataInRecyclerView(it)

    }

    private fun setViewsInSuccess() {
        binding.rvApod.visibility = View.VISIBLE
        binding.pbRvAPOD.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
    }

    private fun loadMoreResults() {
        binding.rvApod.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findLastVisibleItemPositions(null)
                    val total = adapter.itemCount
                    if (!isLoading && (visibleItemCount + pastVisibleItem[pastVisibleItem.lastIndex]) >= total) {
                        getMoreResults(newDates()[0], newDates()[1])
                    }
                }
            }
        })
    }

    private fun configRecyclerView() {
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvApod.layoutManager = layoutManager
    }

    private fun readFromDataStore() {
        dataStore.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            startDate.time = sdf.parse(date)!!
        })
        dataStoreUtils.readValueSearch.observe(viewLifecycleOwner, {
            isFirstSearch = it
            if (it == 0) {
                binding.inputLayoutSearch.helperText =
                    resources.getString(R.string.helper_text_search_view)
            }
        })
    }

    private fun isAdapterInit() {
        if (!::adapter.isInitialized) {
            getAllFromDatabase()
        } else {
            binding.rvApod.adapter = adapter
        }
    }

    private fun newDates(): Array<String> {
        val newEndDate = Calendar.getInstance().apply {
            set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DATE)
            )
            add(Calendar.DATE, -1)
        }
        val newStartDate = Calendar.getInstance().apply {
            set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DATE)
            )
            add(Calendar.DATE, -10)
        }
        return arrayOf(sdf.format(newEndDate.time), sdf.format(newStartDate.time))
    }

    private fun getMoreResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchMoreResults(end, start)
            .observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.pbMoreResults.visibility = View.VISIBLE
                        isSearchMethodsEnabled(false)
                    }
                    is Result.Success -> {
                        binding.pbMoreResults.visibility = View.GONE
                        isSearchMethodsEnabled(true)
                        isLoading = false
                        if (exploreList == result.data) {
                            showErrorSnackbarMessage(getString(R.string.verify_your_connection_internet))
                            return@Observer
                        }
                        exploreList = result.data
                        onSuccessMoreResults(result)
                    }
                    is Result.Failure -> {
                        isSearchMethodsEnabled(true)
                        isLoading = false
                        showErrorSnackbarMessage(getString(R.string.something_went_wrong))

                    }
                }
            })
    }

    private fun onSuccessMoreResults(result: Result.Success<List<APOD>>) {
        adapter.setData(result.data)
        viewModelShared.setAPODList(APODList(adapter.getData()))
        dataStore.saveLastDateToDataStore(result.data[result.data.lastIndex].date)
    }

    override fun onAPODClick(apod: APOD, apodList: List<APOD>, position: Int) {
        if (isLoading && !isSearchResults) {
            isLoading = false
        }
        viewModelShared.setArgsToDetails(DetailsArgs(apod, adapter, position))
        findNavController().navigate(R.id.action_exploreFragment_to_detailsFragment)
    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%$query%"
        isDisableLoadMoreResults(true)
        viewModel.fetchSearchResults(searchQuery).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    //Loading
                }
                is Result.Success -> {
                    if (it.data.isEmpty()) {
                        showErrorMessage(
                            R.drawable.ic_outlined_flag,
                            "${resources.getString(R.string.textNoResultsTitle)} \"$query\"",
                            getString(R.string.textNoResultsSubtitle)
                        )
                        return@Observer
                    }
                    onSuccessResults(it)
                }
                is Result.Failure -> {
                    //Failurex1
                }
            }
        })
    }

    private fun isDisableLoadMoreResults(answer: Boolean) {
        isSearchResults = answer
        isLoading = answer
        viewModelShared.setDisableLoadMoreResults(LoadMoreResults(answer, answer))
    }
}