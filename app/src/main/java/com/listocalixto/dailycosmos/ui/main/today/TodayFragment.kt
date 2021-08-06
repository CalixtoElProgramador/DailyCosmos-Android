package com.listocalixto.dailycosmos.ui.main.today

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.data.local.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.databinding.FragmentTodayBinding
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.apod.APODViewModelFactory
import com.listocalixto.dailycosmos.presentation.apod.DataStoreViewModel
import com.listocalixto.dailycosmos.repository.apod.APODRepositoryImpl
import com.listocalixto.dailycosmos.repository.apod.RetrofitClient
import com.listocalixto.dailycosmos.ui.main.today.adapter.TodayAdapter
import java.text.SimpleDateFormat
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.databinding.ItemApodDailyBinding
import java.util.*
import kotlin.math.abs

private const val MIN_SCALE = 0.75f

@Suppress("DEPRECATION")
class TodayFragment : Fragment(R.layout.fragment_today), TodayAdapter.OnImageAPODClickListener,
    ViewPager2.PageTransformer {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val viewModel by activityViewModels<APODViewModel> {
        APODViewModelFactory(
            APODRepositoryImpl(
                RemoteAPODDataSource(RetrofitClient.webservice),
                LocalAPODDataSource(AppDatabase.getDatabase(requireContext()).apodDao())
            )
        )
    }

    private var endDate: Calendar = Calendar.getInstance()
    private var startDate: Calendar = Calendar.getInstance().apply {
        set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DATE)
        )
        add(Calendar.DATE, -10)
    }

    private lateinit var binding: FragmentTodayBinding
    private lateinit var dataStoreViewModel: DataStoreViewModel
    private lateinit var adapterToday: TodayAdapter
    private lateinit var newStartDate: Calendar
    private var isLoading = false

    private var sizeList: Int = 0

    override fun onResume() {
        super.onResume()
        isLoading = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        configWindow()
        readFromDataStore()
        isAdapterInit()
        configViewPager() //And loadMoreResults when ViewPager.currentItem == results.lastIndex
    }

    private fun isAdapterInit() {
        if (!::adapterToday.isInitialized) {
            getResults(sdf.format(endDate.time), sdf.format(startDate.time))
        } else {
            binding.vpPhotoToday.adapter = adapterToday
        }
    }

    private fun configViewPager() {
        binding.vpPhotoToday.setPageTransformer(this)
    }

    private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS))
    }

    private fun initVars(view: View) {
        binding = FragmentTodayBinding.bind(view)
        dataStoreViewModel =
            ViewModelProvider(requireActivity()).get(DataStoreViewModel::class.java)
    }

    private fun readFromDataStore() {
        dataStoreViewModel.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            startDate.time = sdf.parse(date)!!
        })
        dataStoreViewModel.readSizeListFromDataStore.observe(viewLifecycleOwner, { size ->
            sizeList = size
        })
    }

    private fun getResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchAPODResults(end, start)
            .observe(viewLifecycleOwner, { result ->
                when (result) {
                    is Result.Loading -> {
                        if (!::adapterToday.isInitialized) {
                            binding.lottieLoading.visibility = View.VISIBLE
                            Log.d("ViewModelDaily", "Loading... Adapter is NOT Initialized")
                        } else {
                            Log.d("ViewModelDaily", "Loading... Adapter is Initialized")
                        }
                    }
                    is Result.Success -> {
                        binding.lottieLoading.visibility = View.GONE
                        if (::adapterToday.isInitialized) {
                            sizeList += 10
                            adapterToday.setData(result.data)
                            dataStoreViewModel.saveLastDateToDataStore(result.data[result.data.lastIndex].date)
                            dataStoreViewModel.saveNewSizeListToDataStore(sizeList)
                            Log.d("ViewModelDaily", "Result... Adapter is Initialized")
                        } else {
                            Log.d("ViewModelDaily", "Result... Adapter is NOT Initialized")
                            adapterToday = TodayAdapter(result.data, this@TodayFragment)
                            binding.vpPhotoToday.adapter = adapterToday
                        }
                        isLoading = false
                        Log.d("ViewModelDaily", "Results: ${result.data}")
                    }
                    is Result.Failure -> {
                        binding.lottieLoading.visibility = View.GONE
                        Log.d("ViewModelDaily", "ViewModel error: ${result.exception}")
                    }
                }
            })
    }

    override fun onImageClick(apod: APOD, itemBinding: ItemApodDailyBinding) {
        if (itemBinding.imgApodPicture.drawable == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.wait_for_the_image_to_load),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            val action = TodayFragmentDirections.actionTodayFragmentToPictureFragment(
                apod.hdurl,
                apod.title,
                apod.url
            )
            findNavController().navigate(action)
        }

    }

    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageWidth = width
            when {
                position < -1 -> {
                    alpha = 0f
                }
                position <= 0 -> {
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> {
                    alpha = 1 - position
                    translationX = pageWidth * -position
                    translationZ = -1f
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                }
                else -> {
                    alpha = 0f
                }
            }
        }
        loadMoreResults()
    }

    private fun loadMoreResults() {
        Log.d("ViewPager2", "Position of the ViewPager: ${binding.vpPhotoToday.currentItem}")
        Log.d("ViewPager2", "List size: $sizeList")
        if (!isLoading) {
            Log.d("ViewPager2", "isLoading: $isLoading")
            if (binding.vpPhotoToday.currentItem >= sizeList - 6) {
                getResults(newDates()[0], newDates()[1])
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
        newStartDate = Calendar.getInstance().apply {
            set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DATE)
            )
            add(Calendar.DATE, -10)
        }
        return arrayOf(sdf.format(newEndDate.time), sdf.format(newStartDate.time))
    }
}