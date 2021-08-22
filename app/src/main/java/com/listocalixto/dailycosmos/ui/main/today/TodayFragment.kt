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
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.nl.translate.Translator
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
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.PictureArgs
import java.util.*
import kotlin.math.abs

private const val MIN_SCALE = 0.75f

@Suppress("DEPRECATION")
class TodayFragment : Fragment(R.layout.fragment_today), TodayAdapter.OnImageAPODClickListener,
    ViewPager2.PageTransformer, TodayAdapter.OnFabClickListener,
    TodayAdapter.OnButtonClickListener, TodayAdapter.OnIconClickListener {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DATE, 0)
    }
    private val todayLeastTenDays: Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DATE, -10)
        }
    private val viewModelShared by activityViewModels<MainViewModel>()
    private val dataStoreAPOD by activityViewModels<APODDataStoreViewModel>()
    private val dataStoreTranslator by activityViewModels<TranslatorViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val viewModel by activityViewModels<APODViewModel> {
        APODViewModelFactory(
            APODRepositoryImpl(
                RemoteAPODDataSource(RetrofitClient.webservice),
                LocalAPODDataSource(AppDatabase.getDatabase(requireContext()).apodDao()),
                RemoteAPODFavoriteDataSource(),
                LocalFavoriteDataSource(AppDatabase.getDatabase(requireContext()).favoriteDao())
            )
        )
    }
    private val viewModelFavorite by activityViewModels<APODFavoriteViewModel> {
        APODFavoriteViewModelFactory(
            FavoritesRepoImpl(
                RemoteAPODFavoriteDataSource(),
                LocalFavoriteDataSource(AppDatabase.getDatabase(requireContext()).favoriteDao())
            )
        )
    }

    private var isLoading = false
    private var isNewDate = false
    private var userHaveInternet = true
    private var isAvailableLoadMoreRecentResults = false
    private var isDownloadTheTranslator = -1
    private var isFirstTimeToOpenImage = -1
    private var isNotFirstTimeGetResults = -1
    private var firstRecentResultIndex = 0
    private var lastRecentResultIndex = 0
    private var dateToFind: String? = null
    private var lastResultDateToCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    private var startDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    private lateinit var binding: FragmentTodayBinding
    private lateinit var adapterToday: TodayAdapter
    private lateinit var storedDates: List<String>
    private lateinit var databaseResults: List<APOD>
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onResume() {
        super.onResume()
        isLoading = false
        if (::bottomNavigation.isInitialized && !bottomNavigation.isVisible) {
            showBottomNavView()
        }
        viewModelShared.isUserHaveInternet().value?.let {
            userHaveInternet = it
        }
        viewModelShared.getDateToFind().value?.let { date ->
            dateToFind = date
        }
    }

    override fun onStart() {
        super.onStart()
        viewModelShared.getDateToFind().value?.let { date ->
            dateToFind = date
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTodayBinding.bind(view)
        getResultsFromDatabase()
        getStoredDates()
        configViewPager() //And loadMoreResults when ViewPager.currentItem == adapter.itemCount
        readFromDataStore()

        binding.buttonReload.setOnClickListener {
            getResults(sdf.format(today.time), sdf.format(startDate.time))
        }
        binding.buttonUseWithoutInternet.setOnClickListener {
            fetchResultsFromDatabase()
            viewModelShared.setUserHaveInternet(false)
        }

    }

    private fun getResultsFromDatabase() {
        viewModel.fetchDataFromDatabase().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    databaseResults = it.data
                }
                else -> { /*something*/
                }
            }
        })
    }

    private fun getStoredDates() {
        viewModel.fetchStoredDates().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    storedDates = it.data
                }
                else -> { /*something*/
                }
            }
        })
    }

    private fun configViewPager() {
        binding.vpPhotoToday.setPageTransformer(this)
    }

    private fun readFromDataStore() {
        dataStoreAPOD.readReferenceDate.observe(viewLifecycleOwner, { referenceDate ->
            if (sdf.format(today.time) != referenceDate) {
                isNewDate = true
            }
        })
        dataStoreAPOD.readLastDateFromDataStore.observe(viewLifecycleOwner, { date ->
            startDate.time = sdf.parse(date)!!
            Log.d("StartDate", "StartDate = ${sdf.format(startDate.time)}")
        })
        dataStoreUtils.readValueFirstTimeGetResults.observe(viewLifecycleOwner, {
            isNotFirstTimeGetResults = it
            isAdapterInit()
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
            getResults(sdf.format(today.time), sdf.format(todayLeastTenDays.time))
        } else {
            binding.vpPhotoToday.adapter = adapterToday
        }
    }

    @SuppressLint("CutPasteId")
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
        viewModel.fetchRecentResults(end, start).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> {
                    binding.layoutErrorNoResults.visibility = View.GONE
                    binding.lottieLoading.visibility = View.VISIBLE
                    hideBottomNav()
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    if (it.data.isEmpty()) {
                        binding.layoutErrorNoResults.visibility = View.VISIBLE
                        return@Observer
                    }

                    /* En mantenimineto - Lo de abajo es para verificar si los APODS recientes ya están
                    * almacenados en la base de datos. Esto funciona para cuando el usuario se ausentó
                    * más, igual o menor a 10 días. El detalle aquí, es que para más de 10 días
                    * inactivos, se corre el riesgo de que se pierda fotos si rota la pantalla o activa
                    * el modo oscuro en medio del camino. Hay un bug muy extraño que no lo logro entender,
                    * y es que cuando rota su pantalla, se trae todos los resultados que le hacían falta, TODOS, pero
                    * cuando la vuelvo a enderzar, sólo trae consigo los que se almacenaron en la base de datos,
                    * que curiosamente son los que tenia desde un principio y los que logró cargar antes de
                    * cambiar la configuración del teléfono, ¿dónde quedaron y por qué no se almacenaron los que faltan? No lo sé...
                    * Es algo muy raro y creo que esta solución no es muy buena.
                    * Creo que lo correcto sería tener un servicio tipo WhatsApp, en el que tan pronto se suba un APOD,
                    * notifique al usuario y actualice su base de datos, en vez de estar haciendo estos malabares que
                    * son dificiles de entender y que además son muy frágiles a que se cometan bugs o problemas técnicos.
                    * Por lo pronto, lo dejaré así. Si el usuario se ausenta de la aplicación más de 10 días, pues que
                    * cruce los dedos e intente cargar los que le faltan. En fin, no hay aplicaciones perfectas, así que
                    * me parece bien esta opción. Por lo menos funciona bien para cuando se ausenta igual o menor a 10 días */

                    val recentResults = it.data.take(11)
                    setDataInViewPager(recentResults)
                    val lastResultDate = recentResults[10].date
                    lastResultDateToCalendar.time = sdf.parse(lastResultDate)!!
                    lastResultDateToCalendar.add(Calendar.DATE, -1)
                    if (dateToFind.isNullOrEmpty()) {
                        dateToFind = sdf.format(lastResultDateToCalendar.time)
                        dateToFind?.let { date ->
                            //dataStoreAPOD.saveLastDateToDataStore(date)
                            viewModelShared.setDateToFind(date)
                        }
                    }
                    if (storedDates.contains(dateToFind)) {
                        dataStoreAPOD.saveReferenceDate(sdf.format(today.time))
                        fetchResultsFromDatabase()
                    } else {
                        startDate.time = lastResultDateToCalendar.time
                        firstRecentResultIndex += 10
                        lastRecentResultIndex = firstRecentResultIndex + 11
                        isAvailableLoadMoreRecentResults = true
                    }

                    /* En mantenimineto */

                    bottomNavigation = activity?.findViewById(R.id.bottom_navigation)!!
                    if (!bottomNavigation.isVisible) {
                        showBottomNavView()
                    }
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                    binding.layoutErrorNoResults.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setDataInViewPager(recentResults: List<APOD>) {
        adapterToday = TodayAdapter(recentResults, this, this, this, this)
        binding.vpPhotoToday.adapter = adapterToday
    }

    private fun fetchResultsFromDatabase() {
        viewModel.fetchDataFromDatabase().observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    binding.layoutErrorNoResults.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    adapterToday = TodayAdapter(it.data, this, this, this, this)
                    bottomNavigation = activity?.findViewById(R.id.bottom_navigation)!!
                    binding.vpPhotoToday.adapter = adapterToday
                    if (!bottomNavigation.isVisible) {
                        showBottomNavView()
                    }
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
                    binding.layoutErrorNoResults.visibility = View.GONE
                    binding.lottieLoading.visibility = View.VISIBLE
                    hideBottomNav()
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    if (it.data.isEmpty()) {
                        binding.layoutErrorNoResults.visibility = View.VISIBLE
                        binding.buttonUseWithoutInternet.visibility = View.GONE
                        return@Observer
                    }
                    adapterToday = TodayAdapter(it.data, this, this, this, this)
                    binding.vpPhotoToday.adapter = adapterToday
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

    @SuppressLint("CutPasteId")
    private fun getMoreResults(end: String, start: String) {
        isLoading = true
        viewModel.fetchMoreResults(end, start).observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    //Loading
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
        binding.lottieLoading.visibility = View.GONE
    }

    private fun moreResultsSuccess(it: Result.Success<List<APOD>>) {
        if (isNewDate && isAvailableLoadMoreRecentResults) {
            Log.d("moreResults", "Entró al bloque de traer recientes. ")
            val recentResults = it.data.take(lastRecentResultIndex)
            val moreRecentResults = it.data.subList(firstRecentResultIndex, lastRecentResultIndex)
            val lastResultDate = moreRecentResults[10].date
            lastResultDateToCalendar.time = sdf.parse(lastResultDate)!!
            lastResultDateToCalendar.add(Calendar.DATE, -1)
            dateToFind = sdf.format(lastResultDateToCalendar.time)
            viewModelShared.setDateToFind(dateToFind!!)
            if (storedDates.contains(dateToFind)) {
                isAvailableLoadMoreRecentResults = false
                val total = recentResults + databaseResults
                adapterToday.setData(total)
                dataStoreAPOD.saveLastDateToDataStore(databaseResults[databaseResults.lastIndex].date)
                dataStoreAPOD.saveReferenceDate(sdf.format(today.time))
            } else {
                adapterToday.setData(recentResults)
                startDate.time = sdf.parse(lastResultDate)!!
                firstRecentResultIndex += 10
                lastRecentResultIndex = firstRecentResultIndex + 11
            }
        } else {
            adapterToday.setData(it.data)
            dataStoreAPOD.saveLastDateToDataStore(it.data[it.data.lastIndex].date)
        }
        isLoading = false
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
            "Posición acutal: ${binding.vpPhotoToday.currentItem}, tamaño del adaptador: ${adapterToday.itemCount} "
        )
        if (!isLoading && binding.vpPhotoToday.currentItem >= adapterToday.itemCount - 7) {
            getMoreResults(newDates()[0], newDates()[1])
        }
    }

    private fun newDates(): Array<String> {
        val newEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DATE)
            )
            add(Calendar.DATE, -1)
        }
        val newStartDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
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
}