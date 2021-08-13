package com.listocalixto.dailycosmos.ui.main.today

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.databinding.FragmentTodayBinding
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.apod.APODViewModelFactory
import com.listocalixto.dailycosmos.presentation.preferences.APODDataStoreViewModel
import com.listocalixto.dailycosmos.domain.apod.APODRepositoryImpl
import com.listocalixto.dailycosmos.domain.apod.RetrofitClient
import com.listocalixto.dailycosmos.ui.main.today.adapter.TodayAdapter
import java.text.SimpleDateFormat
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource
import com.listocalixto.dailycosmos.data.remote.translator.TranslatorDataSource
import com.listocalixto.dailycosmos.databinding.ItemApodDailyBinding
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModelFactory
import com.listocalixto.dailycosmos.presentation.preferences.TranslatorViewModel
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepoImpl
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import java.util.*
import kotlin.math.abs

private const val MIN_SCALE = 0.75f

@Suppress("DEPRECATION")
class TodayFragment : Fragment(R.layout.fragment_today), TodayAdapter.OnImageAPODClickListener,
    ViewPager2.PageTransformer, TodayAdapter.OnFabClickListener,
    TodayAdapter.OnButtonClickListener {

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
    private val viewModelFavorite by activityViewModels<APODFavoriteViewModel> {
        APODFavoriteViewModelFactory(
            FavoritesRepoImpl(
                RemoteAPODFavoriteDataSource(),
                LocalFavoriteDataSource(AppDatabase.getDatabase(requireContext()).favoriteDao()),
                LocalAPODDataSource(AppDatabase.getDatabase(requireContext()).apodDao())
            )
        )
    }

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

    private lateinit var binding: FragmentTodayBinding
    private lateinit var dataStore: APODDataStoreViewModel
    private lateinit var translatorViewModel: TranslatorViewModel
    private lateinit var utilsViewModel: UtilsViewModel
    private lateinit var adapterToday: TodayAdapter


    override fun onResume() {
        super.onResume()
        isLoading = false

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        configViewPager() //And loadMoreResults when ViewPager.currentItem == adapter.itemCount
        readFromDataStore()
        isAdapterInit()
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

    private fun initVars(view: View) {
        binding = FragmentTodayBinding.bind(view)
        dataStore =
            ViewModelProvider(requireActivity()).get(APODDataStoreViewModel::class.java)
        translatorViewModel =
            ViewModelProvider(requireActivity()).get(TranslatorViewModel::class.java)
        utilsViewModel = ViewModelProvider(requireActivity()).get(UtilsViewModel::class.java)
    }

    private fun readFromDataStore() {
        dataStore.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            startDate.time = sdf.parse(date)!!
        })
    }

    private fun getResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchAPODResults(end, start)
            .observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Loading -> {
                        if (!::adapterToday.isInitialized) {
                            binding.lottieLoading.visibility = View.VISIBLE
                            Log.d("ViewModelDaily", "Loading... Adapter is NOT Initialized")
                            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.GONE
                        } else {
                            Log.d("ViewModelDaily", "Loading... Adapter is Initialized")
                        }
                    }
                    is Result.Success -> {
                        binding.lottieLoading.visibility = View.GONE
                        if (::adapterToday.isInitialized) {
                            adapterToday.setData(result.data)
                            dataStore.saveLastDateToDataStore(result.data[result.data.lastIndex].date)
                            Log.d("ViewModelDaily", "Result... Adapter is Initialized")
                        } else {
                            Log.d("ViewModelDaily", "Result... Adapter is NOT Initialized")
                            adapterToday =
                                TodayAdapter(
                                    result.data,
                                    this,
                                    this,
                                    this
                                )
                            binding.vpPhotoToday.adapter = adapterToday
                            Handler().postDelayed({
                                activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.apply {
                                    animation = AnimationUtils.loadAnimation(
                                        requireContext(),
                                        R.anim.slide_in_bottom
                                    )
                                    visibility = View.VISIBLE
                                }
                            }, 1500)
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
            utilsViewModel.readValue.observe(viewLifecycleOwner, {
                when (it) {
                    0 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.caution))
                            .setIcon(R.drawable.ic_error_outline)
                            .setMessage(resources.getString(R.string.caution_open_image))
                            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
                            .setNegativeButton(resources.getString(R.string.settings)) { _, _ ->
                                utilsViewModel.saveValue(1)
                                navigateToSettingsActivity()
                            }
                            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                                utilsViewModel.saveValue(1)
                                navigateToPictureFragment(apod)
                            }.show()
                    }
                    1 -> {
                        navigateToPictureFragment(apod)
                    }
                }
            })
        }
    }

    private fun navigateToSettingsActivity() {
        findNavController().navigate(R.id.action_todayFragment_to_settingsActivity)
    }

    private fun navigateToPictureFragment(apod: APOD) {
        val action = TodayFragmentDirections.actionTodayFragmentToPictureFragment(
            apod.hdurl,
            apod.title,
            apod.url
        )
        findNavController().navigate(action)
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
        Log.d("ViewPager2", "List size: ${adapterToday.itemCount}")
        if (!isLoading) {
            Log.d("ViewPager2", "isLoading: $isLoading")
            if (binding.vpPhotoToday.currentItem >= adapterToday.itemCount - 7) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onFabClick(
        apod: APOD,
        itemBinding: ItemApodDailyBinding,
        position: Int,
        apodList: List<APOD>
    ) {
        //viewModel.getAPOD(apod.date)

        when (apod.is_favorite) {
            0 -> {
                apodList[position].is_favorite = 1
                adapterToday.setData(apodList)
                viewModel.updateFavorite(apod, 1)
                viewModelFavorite.setAPODFavorite(apod)
                itemBinding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
            }
            1 -> {
                apodList[position].is_favorite = 0
                adapterToday.setData(apodList)
                viewModel.updateFavorite(apod, 0)
                viewModelFavorite.deleteFavorite(apod)
                itemBinding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
            }
        }
    }

    @SuppressLint("ShowToast")
    override fun onButtonClick(apod: APOD, itemBinding: ItemApodDailyBinding) {
        translatorViewModel.readValue.observe(viewLifecycleOwner, {
            if (it == 0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.ask_download_traductor_title))
                    .setIcon(R.drawable.ic_save_alt)
                    .setMessage(resources.getString(R.string.ask_download_traductor))
                    .setNegativeButton(resources.getString(R.string.no_thanks)) { _, _ ->
                    }
                    .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Snackbar.make(
                                binding.vpPhotoToday,
                                getString(R.string.snackbar_download_translator),
                                Snackbar.LENGTH_SHORT
                            )
                                .setAnchorView(requireActivity().requireViewById(R.id.bottom_navigation))
                                .show()
                        } else {
                            Snackbar.make(
                                binding.vpPhotoToday,
                                getString(R.string.snackbar_download_translator),
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                        translatorViewModel.saveValue(1)
                    }
                    .show()
            } else {
                val translator =
                    TranslatorDataSource().downloadEnglishToOwnerLanguageModel(
                        requireContext(),
                        requireActivity()
                    )
                if (translator == null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.bad_news))
                        .setIcon(R.drawable.ic_sentiment_dissatisfied)
                        .setMessage(resources.getString(R.string.languaje_not_available))
                        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                        }.show()
                } else {
                    translator.translate(apod.title).addOnSuccessListener { titleTranslated ->
                        itemBinding.textApodTitle.text = titleTranslated
                        translator.translate(apod.explanation)
                            .addOnSuccessListener { textTranslated ->
                                itemBinding.textApodExplanation.text = textTranslated
                                translator.close()
                            }
                    }
                }
            }
        })
    }
}