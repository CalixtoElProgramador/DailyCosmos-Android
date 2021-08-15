package com.listocalixto.dailycosmos.ui.main.explore

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.listocalixto.dailycosmos.R
import androidx.lifecycle.Observer
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
import com.listocalixto.dailycosmos.databinding.ItemApodBinding
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ExploreFragment : Fragment(R.layout.fragment_explorer), ExploreAdapter.OnAPODClickListener {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
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

    private var isLoading = false
    private var endDate: Calendar = Calendar.getInstance()
    private var startDate: Calendar = Calendar.getInstance().apply {
        set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DATE)
        )
        add(Calendar.DATE, -10)
    }

    private lateinit var binding: FragmentExplorerBinding
    private lateinit var dataStore: APODDataStoreViewModel
    private lateinit var adapter: ExploreAdapter
    private lateinit var layoutManager: StaggeredGridLayoutManager

    private lateinit var adapterExtra: ExploreAdapter

    override fun onResume() {
        super.onResume()
        isLoading = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        //configWindow()
        configRecyclerView()
        readFromDataStore()
        isAdapterInit()
        loadMoreResults()

        binding.inputLayoutSearch.setEndIconOnClickListener {
            if (binding.inputSearch.text.toString().isEmpty()) {
                adapterExtra = adapter
                binding.rvApod.adapter = adapter
            } else {
                searchDatabase(binding.inputSearch.text.toString())
            }
        }

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.settingsActivity -> {
                    findNavController().navigate(R.id.action_exploreFragment_to_settingsActivity)
                    true
                }
                R.id.calendar -> {
                    openDatePicker()
                    true
                }
                R.id.random -> {
                    getRandomAPODs()
                    true
                }
                else -> {
                    false
                }
            }
        }
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
        viewModel.fetchCalendarResults(end, start).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    Log.d("ViewModel", "Loading calendarResults...")
                    binding.rvApod.visibility = View.GONE
                    binding.pbRvAPOD.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    Log.d("ViewModel", "Results calendarResults... ${it.data}")
                    binding.rvApod.visibility = View.VISIBLE
                    binding.pbRvAPOD.visibility = View.GONE
                    adapterExtra = ExploreAdapter(it.data, this@ExploreFragment)
                    binding.rvApod.adapter = adapterExtra
                }
                is Result.Failure -> {
                    Log.d("ViewModel", "Failure calendarResults... ${it.exception}")
                }
            }
        })
    }

    private fun getRandomAPODs() {
        viewModel.fetchRandomResults("10").observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    Log.d("ViewModel", "Loading random... count isNotEmpty")
                    binding.rvApod.visibility = View.GONE
                    binding.pbRvAPOD.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    Log.d("ViewModel", "Result random... ${it.data}")
                    binding.rvApod.visibility = View.VISIBLE
                    binding.pbRvAPOD.visibility = View.GONE
                    adapterExtra = ExploreAdapter(it.data, this@ExploreFragment)
                    binding.rvApod.adapter = adapterExtra
                }
                is Result.Failure -> {
                    Log.d("ViewModel", "Failure random... ${it.exception}")
                }
            }
        })
    }

    private fun loadMoreResults() {
        binding.rvApod.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    Log.d("RecyclerView", "onScrolled: First condition")
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findLastVisibleItemPositions(null)
                    val total = adapter.itemCount
                    if (!isLoading) {
                        Log.d("RecyclerView", "onScrolled: Second condition")
                        Log.d(
                            "MoreResults",
                            "visibleItemCount = $visibleItemCount, pastVisibleItem = ${pastVisibleItem[pastVisibleItem.lastIndex]}, total = $total "
                        )
                        if ((visibleItemCount + pastVisibleItem[pastVisibleItem.lastIndex]) >= total) {
                            Log.d("RecyclerView", "onScrolled: Third condition")
                            getResults(newDates()[0], newDates()[1])
                        }
                    }
                }
            }
        })
    }

    private fun initVars(view: View) {
        binding = FragmentExplorerBinding.bind(view)
        dataStore =
            ViewModelProvider(requireActivity()).get(APODDataStoreViewModel::class.java)
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
            /*if (sdf.format(referenceDate.time) == sdf.format(startDate.time)) {
                binding.titleCollapsingToolBar.text = getString(R.string.title_explore_collapsing_toolbar)
            }*/
        })
    }

    private fun isAdapterInit() {
        if (!::adapter.isInitialized) {
            getResults(sdf.format(endDate.time), sdf.format(startDate.time))
        } else {
            if (!::adapterExtra.isInitialized) {
                binding.rvApod.adapter = adapter
            } else {
                binding.rvApod.adapter = adapterExtra
            }
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
            .observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Loading -> {
                        if (!::adapter.isInitialized) {
                            binding.pbRvAPOD.visibility = View.VISIBLE
                            Log.d("ViewModel", "Loading... Adapter is NOT Initialized")
                            binding.pbMoreResults.visibility = View.GONE
                        } else {
                            Log.d("ViewModel", "Loading... Adapter is Initialized")
                            binding.pbMoreResults.visibility = View.VISIBLE
                        }
                    }
                    is Result.Success -> {
                        if (::adapter.isInitialized) {
                            adapter.setData(result.data)
                            dataStore.saveLastDateToDataStore(result.data[result.data.lastIndex].date)
                            binding.pbMoreResults.visibility = View.GONE
                            Log.d("ViewModel", "Result... Adapter is Initialized")
                        } else {
                            Log.d("ViewModel", "Result... Adapter is NOT Initialized")
                            isBottomNavVisible()
                            binding.pbRvAPOD.visibility = View.GONE
                            adapter = ExploreAdapter(result.data, this@ExploreFragment)
                            binding.rvApod.adapter = adapter
                        }
                        isLoading = false
                        Log.d("ViewModel", "Results: ${result.data}")
                    }
                    is Result.Failure -> {
                        isBottomNavVisible()
                        Log.d("ViewModel", "ViewModel error: ${result.exception}")

                    }
                }
            })
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

    override fun onAPODClick(apod: APOD, binding: ItemApodBinding) {
        val action = ExploreFragmentDirections.actionExploreFragmentToDetailsFragment(
            apod.copyright,
            apod.date,
            apod.explanation,
            apod.hdurl,
            apod.media_type,
            apod.thumbnail_url,
            apod.title,
            apod.url,
            apod.is_favorite
        )
        findNavController().navigate(action)

    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%$query%"

        viewModel.fetchSearchResults(searchQuery).observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    adapterExtra = ExploreAdapter(it.data, this)
                    binding.rvApod.adapter = adapterExtra
                }
                is Result.Failure -> {
                }
            }
        })
    }
}