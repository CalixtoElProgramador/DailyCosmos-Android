package com.listocalixto.dailycosmos.ui.main.explore

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
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
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.databinding.FragmentExplorerBinding
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.apod.APODViewModelFactory
import com.listocalixto.dailycosmos.presentation.preferences.APODDataStoreViewModel
import com.listocalixto.dailycosmos.domain.apod.APODRepositoryImpl
import com.listocalixto.dailycosmos.domain.apod.RetrofitClient
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.main.details.DetailsArgs
import com.listocalixto.dailycosmos.ui.main.details.DetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ExploreFragment : Fragment(R.layout.fragment_explorer), ExploreAdapter.OnAPODClickListener {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val viewModelDetails by activityViewModels<DetailsViewModel>()
    private val dataStore by activityViewModels<APODDataStoreViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val viewModel by activityViewModels<APODViewModel> {
        APODViewModelFactory(
            APODRepositoryImpl(
                RemoteAPODDataSource(RetrofitClient.webservice),
                LocalAPODDataSource(AppDatabase.getDatabase(requireContext()).apodDao()),
                RemoteAPODFavoriteDataSource()
            )
        )
    }

    private var thisMonthMilliseconds = MaterialDatePicker.thisMonthInUtcMilliseconds()
    private var todayMilliseconds = MaterialDatePicker.todayInUtcMilliseconds()
    private var isFirstSearch = -1
    private var isLoading = false
    private var isSearchResults = false
    private var endDate: Calendar = Calendar.getInstance()
    private var startDate: Calendar = Calendar.getInstance().apply {
        set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DATE)
        )
        add(Calendar.DATE, -10)
    }

    private var referenceDate: Calendar = Calendar.getInstance().apply {
        set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DATE)
        )
        add(Calendar.DATE, -10)
    }

    private lateinit var binding: FragmentExplorerBinding
    private lateinit var adapter: ExploreAdapter
    private lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExplorerBinding.bind(view)
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        //configWindow()
        configRecyclerView()
        readFromDataStore()
        isAdapterInit()
        loadMoreResults()

        bottomNavigationView?.setOnItemReselectedListener { item -> smoothScrollToStart(item) }
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
            false
        }
    }

    private fun getCalendarResults(): Boolean {
        isSearchResults = true
        binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = false
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
            isLoading = false
            isSearchResults = false
            getAllFromDatabase()

        } else {
            searchDatabase(query)
        }
    }

    private fun getAllFromDatabase() {
        viewModel.fetchDataFromDatabase().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    onSuccessDatabaseResults(it)
                }
                is Result.Failure -> {
                }
            }
        })
    }

    private fun onSuccessDatabaseResults(it: Result<List<APOD>>?) {
        setViewsInSuccess()
        setDataInRecyclerView(it as Result.Success<List<APOD>>)
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
        binding.rvApod.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.pbRvAPOD.visibility = View.VISIBLE
    }

    private fun openDatePicker() {
        val constraintsBuilder = buildConstraint()
        val dateRangePicker = configDatePicker(constraintsBuilder)
        dateRangePicker.show(activity?.supportFragmentManager!!, "ExploreFragment")
        eventsDatePicker(dateRangePicker)

    }

    private fun configDatePicker(constraintsBuilder: CalendarConstraints.Builder): MaterialDatePicker<Pair<Long, Long>> {
        return MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .setSelection(
                Pair(thisMonthMilliseconds, todayMilliseconds)
            ).setCalendarConstraints(constraintsBuilder.build())
            .build()
    }

    private fun buildConstraint(): CalendarConstraints.Builder {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        calendar[Calendar.MONTH] = Calendar.JUNE
        calendar[Calendar.YEAR] = 1995
        val june1995 = calendar.timeInMillis

        return CalendarConstraints.Builder()
            .setStart(june1995)
            .setEnd(today)
    }

    @SuppressLint("ShowToast", "ResourceAsColor")
    private fun eventsDatePicker(dateRangePicker: MaterialDatePicker<Pair<Long, Long>>) {
        dateRangePicker.addOnCancelListener {
            binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = true
        }
        dateRangePicker.addOnDismissListener {
            binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = true
        }
        dateRangePicker.addOnNegativeButtonClickListener {
            binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = true
        }

        dateRangePicker.addOnPositiveButtonClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val firstDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val secondDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            todayCalendar.timeInMillis = today
            todayCalendar[Calendar.DATE] = todayCalendar[Calendar.DATE] + 1
            firstDay.timeInMillis = it.first
            firstDay[Calendar.DATE] = firstDay[Calendar.DATE] + 1
            secondDay.timeInMillis = it.second
            secondDay[Calendar.DATE] = secondDay[Calendar.DATE] + 1

            if (firstDay.timeInMillis <= todayCalendar.timeInMillis &&
                secondDay.timeInMillis <= todayCalendar.timeInMillis
            ) {
                getCalendarResults(sdf.format(firstDay.time), sdf.format(secondDay.time))
                thisMonthMilliseconds = dateRangePicker.selection?.first!!
                todayMilliseconds = dateRangePicker.selection?.second!!
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Snackbar.make(
                        binding.rvApod,
                        getString(R.string.error_request_future_dates_today),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(requireActivity().requireViewById(R.id.bottom_navigation))
                        .setBackgroundTint(requireContext().resources.getColor(R.color.colorError))
                        .show()
                } else {
                    Snackbar.make(
                        binding.rvApod,
                        getString(R.string.error_request_future_dates_today),
                        Snackbar.LENGTH_LONG
                    )
                        .setBackgroundTint(requireContext().resources.getColor(R.color.colorError))
                        .show()
                }
            }
        }
    }

    private fun getCalendarResults(start: String, end: String) {
        viewModel.fetchCalendarResults(end, start).observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    setViewsInLoading()
                }
                is Result.Success -> {
                    onSuccessCalendarResults(it)
                }
                is Result.Failure -> {
                    isLoading = true
                    binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = true
                    Log.d("ViewModel", "Failure calendarResults... ${it.exception}")
                }
            }
        })
    }

    private fun onSuccessCalendarResults(it: Result.Success<List<APOD>>) {
        setViewsInSuccess()
        setDataInRecyclerView(it)
        isLoading = true
        binding.topAppBar.menu.findItem(R.id.calendar).isEnabled = true
    }

    private fun setDataInRecyclerView(it: Result.Success<List<APOD>>) {
        adapter = ExploreAdapter(it.data, this@ExploreFragment)
        binding.rvApod.adapter = adapter
    }

    private fun getRandomResults(): Boolean {
        isSearchResults = true
        viewModel.fetchRandomResults("10").observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    onLoadingRandomResults()
                }
                is Result.Success -> {
                    onSuccessRandomResults(it)
                }
                is Result.Failure -> {
                    isLoading = true
                    binding.topAppBar.menu.findItem(R.id.random).isEnabled = true
                }
            }
        })

        return true
    }

    private fun onLoadingRandomResults() {
        binding.topAppBar.menu.findItem(R.id.random).isEnabled = false
        Log.d("ViewModel", "Loading random... count isNotEmpty")
        setViewsInLoading()
    }

    private fun onSuccessRandomResults(it: Result.Success<List<APOD>>) {
        isLoading = true
        setViewsInSuccess()
        setDataInRecyclerView(it)
        Log.d("ViewModel", "Result random... ${it.data}")
        binding.topAppBar.menu.findItem(R.id.random).isEnabled = true
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
                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItem[pastVisibleItem.lastIndex]) >= total) {
                            getResults(newDates()[0], newDates()[1])
                        }
                    }
                }
            }
        })
    }

    private fun configRecyclerView() {
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvApod.layoutManager = layoutManager
    }

    /*private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity?.window?.statusBarColor =
            requireActivity().resources.getColor(R.color.colorPrimaryVariant)
    }*/

    private fun readFromDataStore() {
        dataStore.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            startDate.time = sdf.parse(date)!!
            if (sdf.format(referenceDate.time) == sdf.format(startDate.time)) {
                binding.titleCollapsingToolBar.text =
                    getString(R.string.title_explore_collapsing_toolbar)
            }
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
            getResults(sdf.format(endDate.time), sdf.format(startDate.time))
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

    private fun getResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchAPODResults(end, start)
            .observe(viewLifecycleOwner, { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.inputLayoutSearch.isEnabled = false
                        if (!::adapter.isInitialized) {
                            Log.d("ViewModel", "Loading... Adapter is NOT Initialized")
                            setViewsInLoading()
                        } else {
                            Log.d("ViewModel", "Loading... Adapter is Initialized")
                            binding.pbMoreResults.visibility = View.VISIBLE
                        }
                    }
                    is Result.Success -> {
                        binding.inputLayoutSearch.isEnabled = true
                        if (::adapter.isInitialized) {
                            adapter.setData(result.data)
                            dataStore.saveLastDateToDataStore(result.data[result.data.lastIndex].date)
                            binding.pbMoreResults.visibility = View.GONE
                            Log.d("ViewModel", "Result... Adapter is Initialized")
                        } else {
                            Log.d("ViewModel", "Result... Adapter is NOT Initialized")
                            onSuccessAPODResults(result)
                        }
                        isLoading = false
                        Log.d("ViewModel", "Results: ${result.data}")
                    }
                    is Result.Failure -> {
                        binding.inputLayoutSearch.isEnabled = true
                        isBottomNavVisible()
                        Log.d("ViewModel", "ViewModel error: ${result.exception}")

                    }
                }
            })
    }

    private fun onSuccessAPODResults(result: Result<List<APOD>>?) {
        isBottomNavVisible()
        setViewsInSuccess()
        setDataInRecyclerView(result as Result.Success<List<APOD>>)
    }

    private fun isBottomNavVisible() {
        if (!activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible!!) {
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.apply {
                    animation = AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.slide_in_bottom
                    )
                    visibility = View.VISIBLE
                }
        }
    }

    override fun onAPODClick(apod: APOD, apodList: List<APOD>, position: Int) {
        if (isLoading && !isSearchResults) {
            isLoading = false
        }
        viewModelDetails.setArgs(DetailsArgs(apod, adapter, position))
        findNavController().navigate(R.id.action_exploreFragment_to_detailsFragment)
    }

    @SuppressLint("SetTextI18n")
    private fun searchDatabase(query: String?) {
        val searchQuery = "%$query%"
        viewModel.fetchSearchResults(searchQuery).observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    if (it.data.isEmpty()) {
                        binding.layoutNoResults.visibility = View.VISIBLE
                        binding.textNoResultsFor.text =
                            "${resources.getString(R.string.textNoResultsTitle)} \"$query\""
                    } else {
                        binding.layoutNoResults.visibility = View.GONE
                    }
                    adapter = ExploreAdapter(it.data, this)
                    binding.rvApod.adapter = adapter
                    isSearchResults = true
                    isLoading = true
                }
                is Result.Failure -> {
                }
            }
        })
    }
}