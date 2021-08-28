package com.listocalixto.dailycosmos.ui.main.today

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentTodayBinding
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.preferences.APODDataStoreViewModel
import com.listocalixto.dailycosmos.ui.main.today.adapter.TodayAdapter
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.nl.translate.Translator
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.remote.translator.TranslatorDataSource
import com.listocalixto.dailycosmos.databinding.ItemApodDailyBinding
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.preferences.TranslatorViewModel
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.main.DateRange
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.PictureArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs

private const val MIN_SCALE = 0.75f
private const val MILLISECONDS_IN_A_DAY = 86400000

@Suppress("DEPRECATION")
@AndroidEntryPoint
class TodayFragment : Fragment(R.layout.fragment_today), TodayAdapter.OnImageAPODClickListener,
    ViewPager2.PageTransformer, TodayAdapter.OnFabClickListener,
    TodayAdapter.OnButtonClickListener, TodayAdapter.OnIconClickListener {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val referenceDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    private val dateOfLastResult: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    private val today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DATE, 0)
    }
    private val todayLeastTenDays: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DATE, -10)
    }
    private val viewModel by activityViewModels<APODViewModel>()
    private val viewModelFavorite by activityViewModels<APODFavoriteViewModel>()
    private val viewModelShared by activityViewModels<MainViewModel>()
    private val dataStoreAPOD by activityViewModels<APODDataStoreViewModel>()
    private val dataStoreTranslator by activityViewModels<TranslatorViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()

    private var isLoading = false
    private var isNewDate = false
    private var userHaveInternet = true
    private var isDownloadTheTranslator: Int = -1
    private var isFirstTimeToOpenImage: Int = -1
    private var isNotFirstTimeGetResults: Int = -1
    private var delta: Int = 0
    private var isFirstTimeOpenTheApp = true
    private var endDate = sdf.format(today.time)
    private var startDate = sdf.format(todayLeastTenDays.time)

    private lateinit var binding: FragmentTodayBinding
    private lateinit var adapterToday: TodayAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModelShared.isUserHaveInternet().value?.let {
            userHaveInternet = it
        }
        viewModelShared.getDelta().value?.let {
            delta = it
        }
        viewModelShared.getDateRange().value?.let { dateRange ->
            endDate = dateRange.endDate
            startDate = dateRange.startDate
        }
        viewModelShared.isFirstTimeOpen().value?.let { answer ->
            isFirstTimeOpenTheApp = answer
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        isLoading = false
        if (::bottomNavigation.isInitialized && !bottomNavigation.isVisible) {
            showBottomNavView()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTodayBinding.bind(view)
        configViewPager() //And loadMoreResults when ViewPager.currentItem == adapter.itemCount
        if (!isFirstTimeOpenTheApp) {
            isAdapterInit()
        }
        readFromDataStore()

        binding.buttonReload.setOnClickListener {
            getResults(endDate, startDate)
        }
        binding.buttonUseWithoutInternet.setOnClickListener {
            fetchResultsFromDatabase()
            viewModelShared.setUserHaveInternet(false)
        }

    }

    private fun configViewPager() {
        binding.vpPhotoToday.setPageTransformer(this)
    }

    private fun readFromDataStore() {
        dataStoreAPOD.readReferenceDate.observe(viewLifecycleOwner) { date ->
            referenceDate.time = sdf.parse(date)!!
            if (sdf.format(today.time) != date) {
                isNewDate = true
            }
        }
        dataStoreUtils.readValueFirstTimeGetResults.observe(viewLifecycleOwner) {
            isNotFirstTimeGetResults = it
            if (isFirstTimeOpenTheApp) {
                isAdapterInit()
            }
        }
        dataStoreAPOD.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            dateOfLastResult.time = sdf.parse(date)!!
        })
        dataStoreTranslator.readValue.observe(viewLifecycleOwner, {
            isDownloadTheTranslator = it
        })
        dataStoreUtils.readValue.observe(viewLifecycleOwner, {
            isFirstTimeToOpenImage = it
        })

    }

    private fun isAdapterInit() {
        if (!::adapterToday.isInitialized) {
            getResults(endDate, startDate)
        } else {
            binding.vpPhotoToday.adapter = adapterToday
        }
    }

    private fun getResults(end: String, start: String) {
        when (isNotFirstTimeGetResults) {
            0 -> {
                fetchResultsFromWebServiceAndFireStore(end, start)
            }
            1 -> {
                if (isNewDate && userHaveInternet) {
                    fetchRecentResultsFromWebService(end, start)

                } else {
                    fetchResultsFromDatabase()
                }
            }
        }
    }

    private fun fetchRecentResultsFromWebService(end: String, start: String) {
        Log.d("fetchRecentResults", "Las fechas solicitadas son: $end, y $start ")
        Log.d("fetchRecentResults", "El nuevo valor del entero es: $delta ")
        viewModel.fetchRecentResults(end, start).observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    isThereAProblem(false)
                    Log.d("fetchRecentResults", "Loading... ")
                }
                is Result.Success -> {
                    val results = result.data
                    if (isResultsEmpty(results)) return@Observer
                    val recentResults = results.take(-delta + 11)
                    val lastResultDateString = recentResults[-delta + 10].date
                    Log.d("fetchRecentResults", "La fecha del último elemento: $lastResultDateString ")
                    val lastResultDateCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    lastResultDateString.let { lastResultDateCalendar.time = sdf.parse(it)!! }
                    Log.d("fetchRecentResults", "Fecha del último resultado: $lastResultDateString")
                    val lastResultDateMilliseconds = lastResultDateCalendar.timeInMillis
                    val referenceDateMilliseconds = referenceDate.timeInMillis
                    Log.d("fetchRecentResults", "$referenceDateMilliseconds - $lastResultDateMilliseconds = ${referenceDateMilliseconds - lastResultDateMilliseconds} ")
                    if (lastResultDateMilliseconds - referenceDateMilliseconds > MILLISECONDS_IN_A_DAY) {
                        keepBringingMoreRecentResults()
                    } else {
                        Log.d("fetchRecentResults", "Todos los resultados nuevos han sido traídos")
                        dataStoreAPOD.saveReferenceDate(sdf.format(today.time))
                        fetchResultsFromDatabase()
                    }

                }
                is Result.Failure -> {
                    isThereAProblem(true)
                    Log.d("fetchRecentResults", "Happen an error: ${result.exception} ")
                }
            }
        })
    }

    private fun isResultsEmpty(results: List<APOD>): Boolean {
        if (results.isEmpty()) {
            isThereAProblem(true)
            return true
        }
        return false
    }

    private fun keepBringingMoreRecentResults() {
        delta -= 10
        viewModelShared.setDelta(delta)

        val newEndDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DATE, delta)
        }
        val newStartDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DATE, delta - 10)
        }

        val newEndDateString = sdf.format(newEndDate.time)
        val newStartDateString = sdf.format(newStartDate.time)
        viewModelShared.setDateRange(DateRange(newEndDateString, newStartDateString))

        Log.d("fetchRecentResults", "Volviendo a llamar el mismo método")
        fetchRecentResultsFromWebService(newEndDateString, newStartDateString)
    }

    private fun fetchResultsFromDatabase() {
        viewModel.fetchDataFromDatabase().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    binding.layoutErrorNoResults.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    setDataInViewPager(it.data)
                    bottomNavigation = activity?.findViewById(R.id.bottom_navigation)!!
                    if (!bottomNavigation.isVisible) {
                        showBottomNavView()
                    }
                    isFirstTimeOpenTheApp = false
                    viewModelShared.setFirstTimeOpen(false)
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                }
            }
        })
    }

    private fun fetchResultsFromWebServiceAndFireStore(end: String, start: String) {
        viewModel.fetchFirstTimeResults(end, start).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    isThereAProblem(false)
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    val results = it.data
                    if (isResultsEmpty(results)) return@Observer
                    setDataInViewPager(it.data)
                    bottomNavigation = activity?.findViewById(R.id.bottom_navigation)!!
                    if (!bottomNavigation.isVisible) {
                        showBottomNavView()
                    }
                    dataStoreAPOD.saveReferenceDate(sdf.format(today.time))
                    dataStoreUtils.saveValueFirstTimeGetResults(1)
                    isNotFirstTimeGetResults = 1
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                }
            }
        })
    }

    private fun isThereAProblem(answer: Boolean) {
        if (answer) {
            binding.lottieLoading.visibility = View.GONE
            binding.layoutErrorNoResults.visibility = View.VISIBLE
        } else {
            binding.lottieLoading.visibility = View.VISIBLE
            binding.layoutErrorNoResults.visibility = View.GONE
            hideBottomNav()
        }

    }

    private fun setDataInViewPager(results: List<APOD>) {
        adapterToday = TodayAdapter(
            results,
            this@TodayFragment,
            this@TodayFragment,
            this@TodayFragment,
            this@TodayFragment
        )
        binding.vpPhotoToday.adapter = adapterToday
    }

    @SuppressLint("CutPasteId")
    private fun getMoreResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchMoreResults(end, start).observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> { /*Loading*/
                }
                is Result.Success -> {
                    moreResultsSuccess(it)
                }
                is Result.Failure -> {
                    moreResultsFailure()
                }
            }
        })
    }

    private fun moreResultsFailure() {
        showErrorSnackbarMessage(getString(R.string.verify_your_connection_internet))
    }

    @SuppressLint("ShowToast")
    private fun showErrorSnackbarMessage(message: String) {
        Snackbar.make(binding.vpPhotoToday, message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottomNavigation)
            .setBackgroundTint(requireContext().resources.getColor(R.color.red_alpha_100))
            .show()
    }

    private fun moreResultsSuccess(it: Result.Success<List<APOD>>) {
        isLoading = false
        adapterToday.setData(it.data)
        dataStoreAPOD.saveLastDateToDataStore(it.data[it.data.lastIndex].date)

    }

    private fun hideBottomNav() {
        if (activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible!!) {
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
                View.GONE
        }
    }

    private fun showBottomNavView() {
        bottomNavigation.apply {
            animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_bottom
            )
            visibility = View.VISIBLE
        }
    }

    override fun onImageClick(apod: APOD, itemBinding: ItemApodDailyBinding) {
        if (itemBinding.imgApodPicture.drawable == null) {
            showToastWaitToLoadImage()
        } else {
            if (apod.media_type != "video" && bottomNavigation.isVisible) {
                when (isFirstTimeToOpenImage) {
                    0 -> {
                        showDialogWarningForResolutionImages(apod)
                    }
                    1 -> {
                        navigateToPictureFragment(apod)
                    }
                }
            }
        }
    }

    private fun showDialogWarningForResolutionImages(apod: APOD) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.caution))
            .setIcon(R.drawable.ic_error_outline)
            .setMessage(resources.getString(R.string.caution_open_image))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setNegativeButton(resources.getString(R.string.settings)) { _, _ ->
                dataStoreUtils.saveValue(1)
                isFirstTimeToOpenImage = 1
                navigateToSettingsActivity()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                dataStoreUtils.saveValue(1)
                isFirstTimeToOpenImage = 1
                navigateToPictureFragment(apod)
            }.show()
    }

    private fun showToastWaitToLoadImage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.wait_for_the_image_to_load),
            Toast.LENGTH_SHORT
        )
            .show()
    }

    private fun navigateToSettingsActivity() {
        findNavController().navigate(R.id.action_todayFragment_to_settingsActivity)
    }

    private fun navigateToPictureFragment(apod: APOD) {
        viewModelShared.setArgsToPicture(PictureArgs(apod.hdurl, apod.url, apod.title))
        findNavController().navigate(R.id.action_todayFragment_to_pictureFragment)
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
        Log.d(
            "ViewPager",
            "La posición actual es: ${binding.vpPhotoToday.currentItem + 1}, tamaño del adaptador: ${adapterToday.itemCount} "
        )
        if (!isLoading && binding.vpPhotoToday.currentItem >= adapterToday.itemCount - 7) {
            getMoreResults(newDates()[0], newDates()[1])
        }
    }

    private fun newDates(): Array<String> {
        val newEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(
                dateOfLastResult.get(Calendar.YEAR),
                dateOfLastResult.get(Calendar.MONTH),
                dateOfLastResult.get(Calendar.DATE)
            )
            add(Calendar.DATE, -1)
        }
        val newStartDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(
                dateOfLastResult.get(Calendar.YEAR),
                dateOfLastResult.get(Calendar.MONTH),
                dateOfLastResult.get(Calendar.DATE)
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
        when (isDownloadTheTranslator) {
            0 -> {
                permissionToDownloadTranslator()
            }
            1 -> {
                val translator = TranslatorDataSource().downloadEnglishToOwnerLanguageModel(
                    requireContext(),
                    requireActivity()
                )
                if (translator == null) {
                    showDialogTranslatorNotExits()
                    return
                } else {
                    translator.translate("a").addOnFailureListener {
                        showSnackbarMessage(getString(R.string.wait_to_translator_download_is_finish))
                    }.addOnSuccessListener {
                        showSnackbarMessage(getString(R.string.translating))
                        translateTitleAndExplanation(translator, apod, itemBinding)
                        itemBinding.textShowOriginal.setOnClickListener {
                            showOriginalText(itemBinding, apod)
                        }
                    }
                }
            }
        }
    }

    private fun translateTitleAndExplanation(
        translator: Translator,
        apod: APOD,
        itemBinding: ItemApodDailyBinding
    ) {
        translator.translate(apod.title).addOnSuccessListener { titleTranslated ->
            itemBinding.textApodTitle.text = titleTranslated
        }

        translator.translate(apod.explanation).addOnSuccessListener { textTranslated ->
            itemBinding.textApodExplanation.text = textTranslated
            translator.close()
        }
        showTextViewShowOriginal(itemBinding)
    }

    private fun showDialogTranslatorNotExits() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.bad_news))
            .setIcon(R.drawable.ic_sentiment_dissatisfied)
            .setMessage(resources.getString(R.string.languaje_not_available))
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            }.show()
    }

    private fun showTextViewShowOriginal(itemBinding: ItemApodDailyBinding) {
        Handler().postDelayed({
            itemBinding.textShowOriginal.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.fade_in_main
                )
                visibility = View.VISIBLE
            }
        }, 400)
    }

    private fun showOriginalText(itemBinding: ItemApodDailyBinding, apod: APOD) {
        itemBinding.textApodTitle.text = apod.title
        itemBinding.textApodExplanation.text = apod.explanation
        hideTextViewShowOriginal(itemBinding)
    }

    private fun hideTextViewShowOriginal(itemBinding: ItemApodDailyBinding) {
        Handler().postDelayed({
            itemBinding.textShowOriginal.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.fade_out_main
                )
                visibility = View.GONE
            }
        }, 400)
    }

    @SuppressLint("ShowToast")
    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.vpPhotoToday, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(bottomNavigation)
            .show()
    }

    private fun permissionToDownloadTranslator() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.ask_download_traductor_title))
            .setIcon(R.drawable.ic_save_alt)
            .setMessage(resources.getString(R.string.ask_download_traductor))
            .setNegativeButton(resources.getString(R.string.no_thanks)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                TranslatorDataSource().downloadEnglishToOwnerLanguageModel(
                    requireContext(),
                    requireActivity()
                )
                showSnackbarMessage(getString(R.string.snackbar_download_translator))
                dataStoreTranslator.saveValue(1)
                isDownloadTheTranslator = 1
            }
            .show()
    }

    override fun onIconClick(apod: APOD) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Video link", apod.url)
        clipboard.setPrimaryClip(clip)
        showSnackbarMessage(getString(R.string.link_copied))
    }

    override fun onDestroy() {
        viewModelShared.setFirstTimeOpen(true)
        super.onDestroy()
    }

}