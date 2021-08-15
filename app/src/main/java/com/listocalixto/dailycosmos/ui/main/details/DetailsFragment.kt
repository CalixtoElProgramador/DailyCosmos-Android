package com.listocalixto.dailycosmos.ui.main.details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.data.local.apod.LocalAPODDataSource
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.data.remote.apod.RemoteAPODDataSource
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource
import com.listocalixto.dailycosmos.data.remote.translator.TranslatorDataSource
import com.listocalixto.dailycosmos.databinding.FragmentDetailsBinding
import com.listocalixto.dailycosmos.domain.apod.APODRepositoryImpl
import com.listocalixto.dailycosmos.domain.apod.RetrofitClient
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepoImpl
import com.listocalixto.dailycosmos.presentation.apod.APODViewModel
import com.listocalixto.dailycosmos.presentation.apod.APODViewModelFactory
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModelFactory
import com.listocalixto.dailycosmos.presentation.preferences.TranslatorViewModel
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter

@Suppress("DEPRECATION")
class DetailsFragment : Fragment(R.layout.fragment_details) {

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
    private val viewModelDetails by activityViewModels<DetailsViewModel>()

    private var position: Int = -1
    private var adapterExplore: ExploreAdapter? = null

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var apodReceived: APOD
    private lateinit var translatorDataStore: TranslatorViewModel
    private lateinit var utilsDataStore: UtilsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.apply {
            animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_out_bottom
            )
            visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        getArgsFromViewModel()
        getValueFavorite()
        updateViews()

        binding.fabAddAPODFavorites.setOnClickListener { updateFavorite() }
        binding.btnTranslate.setOnClickListener { translateExplanation() }
        binding.imgApodPicture.setOnClickListener { verifyDrawable() }

    }

    private fun initVars(view: View) {
        binding = FragmentDetailsBinding.bind(view)
        translatorDataStore =
            ViewModelProvider(requireActivity()).get(TranslatorViewModel::class.java)
        utilsDataStore = ViewModelProvider(requireActivity()).get(UtilsViewModel::class.java)
    }

    private fun getArgsFromViewModel() {
        viewModelDetails.getArgs().value?.let { args ->
            apodReceived = args.apod
            adapterExplore = args.adapterExplore
            position = args.position
        }
    }

    private fun getValueFavorite() {
        viewModelDetails.getFavValue().value?.let {
            apodReceived.is_favorite = it
        }
    }

    private fun updateFavorite() {
        when (apodReceived.is_favorite) {
            0 -> {
                notifyItemChanged(1)
            }
            1 -> {
                notifyItemChanged(0)
            }
            -1 -> {
                notifyItemChanged(1)
            }
        }
    }

    private fun notifyItemChanged(isFavorite: Int) {
        setDrawableOnFAB(isFavorite)
        viewModel.updateFavorite(apodReceived, isFavorite)
        viewModelDetails.setFavValue(isFavorite)
        getValueFavorite()
        adapterExplore?.notifyItemChanged(position)
        when(isFavorite) {
            -1 -> { }
            0 -> { viewModelFavorite.deleteFavorite(apodReceived) }
            1 -> { viewModelFavorite.setAPODFavorite(apodReceived) }
        }
    }

    @SuppressLint("ShowToast")
    private fun translateExplanation() {
        translatorDataStore.readValue.observe(viewLifecycleOwner, {
            if (it == 0) {
                showDialog()
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
                    Snackbar.make(binding.btnTranslate, "Translating...", Snackbar.LENGTH_SHORT)
                        .show()

                    translator.translate(apodReceived.title).addOnSuccessListener { titleTranslated ->
                        binding.textApodTitle.text = titleTranslated
                        translator.translate(apodReceived.explanation)
                            .addOnSuccessListener { textTranslated ->
                                binding.textApodExplanation.text = textTranslated
                                translator.close()
                                Handler().postDelayed({
                                    binding.textShowOriginal.apply {
                                        animation = AnimationUtils.loadAnimation(
                                            requireContext(),
                                            R.anim.fade_in_main
                                        )
                                        visibility = View.VISIBLE
                                    }
                                }, 400)

                                binding.textShowOriginal.setOnClickListener {
                                    binding.textApodTitle.text = apodReceived.title
                                    binding.textApodExplanation.text = apodReceived.explanation

                                    Handler().postDelayed({
                                        binding.textShowOriginal.apply {
                                            animation = AnimationUtils.loadAnimation(
                                                requireContext(),
                                                R.anim.fade_out_main
                                            )
                                            visibility = View.GONE
                                        }
                                    }, 400)
                                }
                            }
                    }
                }
            }
        })
    }

    @SuppressLint("ShowToast")
    private fun showDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
            .setTitle(getString(R.string.ask_download_traductor_title))
            .setIcon(R.drawable.ic_save_alt)
            .setMessage(resources.getString(R.string.ask_download_traductor))
            .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Snackbar.make(
                        binding.imgApodPicture,
                        getString(R.string.snackbar_download_translator),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(requireActivity().requireViewById(R.id.bottom_navigation))
                        .show()
                }
                translatorDataStore.saveValue(1)
            }
            .show()
    }

    private fun verifyDrawable() {
        if (binding.imgApodPicture.drawable == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.wait_for_the_image_to_load),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            utilsDataStore.readValue.observe(viewLifecycleOwner,{
                if (it == 0) {
                    showDialogBeforeOpenImage()
                    return@observe
                } else {
                    navigateToPictureFragment()
                }
            })
        }

    }

    private fun showDialogBeforeOpenImage() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.caution))
            .setIcon(R.drawable.ic_error_outline)
            .setMessage(resources.getString(R.string.caution_open_image))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setNegativeButton(resources.getString(R.string.settings)) { _, _ ->
                navigateToSettingsActivity()
                utilsDataStore.saveValue(1)
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                navigateToPictureFragment()
                utilsDataStore.saveValue(1)
            }.show()
    }

    private fun navigateToSettingsActivity() {
        findNavController().navigate(R.id.action_detailsFragment_to_settingsActivity)
    }

    private fun navigateToPictureFragment() {
        val action = DetailsFragmentDirections.actionDetailsFragmentToPictureFragment(
            apodReceived.hdurl,
            apodReceived.title,
            apodReceived.url
        )
        findNavController().navigate(action)
    }

    private fun updateViews() {
        setImage()
        setTexts()
        setDrawableOnFAB(apodReceived.is_favorite)
    }

    @SuppressLint("SetTextI18n")
    private fun setTexts() {
        binding.textApodTitle.text = apodReceived.title
        binding.textApodDate.text = apodReceived.date
        if (apodReceived.explanation.isEmpty()) {
            binding.textApodExplanation.text = getString(R.string.no_description)
        } else {
            binding.textApodExplanation.text = apodReceived.explanation
        }
        if (apodReceived.copyright.isEmpty()) {
            binding.textApodCopyright.visibility = View.GONE
        } else {
            binding.textApodCopyright.text = "Copyright: ${apodReceived.copyright}"
        }
    }

    private fun setImage() {
        when (apodReceived.media_type) {
            "image" -> {
                if (apodReceived.hdurl.isEmpty()) {
                    Glide.with(requireContext()).load(apodReceived.url).into(binding.imgApodPicture)
                } else {
                    Glide.with(requireContext()).load(apodReceived.hdurl)
                        .into(binding.imgApodPicture)
                }
            }
        }
    }

    private fun setDrawableOnFAB(isFavorite: Int) {
        when (isFavorite) {
            0 -> {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
            }
            1 -> {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
            }
        }
    }

    override fun onDestroy() {
        viewModelDetails.setFavValue(null)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.apply {
            animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_bottom
            )
            visibility = View.VISIBLE
        }
        super.onDestroy()
    }

}