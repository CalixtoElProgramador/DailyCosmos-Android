package com.listocalixto.dailycosmos.ui.main.details

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.nl.translate.Translator
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
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.PictureArgs
import com.listocalixto.dailycosmos.ui.main.explore.adapter.ExploreAdapter

@Suppress("DEPRECATION")
class DetailsFragment : Fragment(R.layout.fragment_details) {

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
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val dataStoreTranslator by activityViewModels<TranslatorViewModel>()
    private val viewModelShared by activityViewModels<MainViewModel>()

    private var position: Int = -1
    private var adapterExplore: ExploreAdapter? = null
    private var isDownloadTheTranslator: Int = -1
    private var isFirstTimeToOpenImage: Int = -1

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var apodReceived: APOD

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bottomNavigation =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!
        if (bottomNavigation.isVisible) {
            bottomNavigation.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.slide_out_bottom
                )
                visibility = View.GONE
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailsBinding.bind(view)
        readFromDataStore()
        getArgsFromViewModel()
        updateViews()

        binding.fabAddAPODFavorites.setOnClickListener { updateFavorite() }
        binding.btnTranslate.setOnClickListener { translateExplanation() }
        binding.imgApodPicture.setOnClickListener { verifyDrawable() }
        binding.iconCopyLink.setOnClickListener { copyLinkToClipboard() }

    }

    @SuppressLint("ShowToast")
    private fun copyLinkToClipboard() {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Video link", apodReceived.url)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.imgApodPicture, "Link copied", Snackbar.LENGTH_SHORT).show()
    }

    private fun readFromDataStore() {
        dataStoreTranslator.readValue.observe(viewLifecycleOwner, {
            isDownloadTheTranslator = it
        })
        dataStoreUtils.readValue.observe(viewLifecycleOwner, {
            isFirstTimeToOpenImage = it
        })
    }

    private fun getArgsFromViewModel() {
        viewModelShared.getArgsToDetails().value?.let { args ->
            apodReceived = args.apod
            adapterExplore = args.adapterExplore
            position = args.position
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
        when (isFavorite) {
            0 -> {
                viewModelFavorite.deleteFavorite(apodReceived)
            }
            1 -> {
                viewModelFavorite.setAPODFavorite(apodReceived)
            }
        }
        setDrawableOnFAB(isFavorite)
        viewModel.updateFavorite(apodReceived, isFavorite)
        apodReceived.is_favorite = isFavorite
        adapterExplore?.notifyItemChanged(position)
    }

    @SuppressLint("ShowToast")
    private fun translateExplanation() {
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
                        translateTitleAndExplanation(translator, apodReceived)
                        binding.textShowOriginal.setOnClickListener {
                            showOriginalText(apodReceived)
                        }
                    }
                }
            }
        }
    }

    private fun showOriginalText(apod: APOD) {
        binding.textApodTitle.text = apod.title
        binding.textApodExplanation.text = apod.explanation
        hideTextViewShowOriginal()
    }

    private fun hideTextViewShowOriginal() {
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

    private fun translateTitleAndExplanation(
        translator: Translator,
        apod: APOD
    ) {
        translator.translate(apod.title).addOnSuccessListener { titleTranslated ->
            binding.textApodTitle.text = titleTranslated
        }

        translator.translate(apod.explanation).addOnSuccessListener { textTranslated ->
            binding.textApodExplanation.text = textTranslated
            translator.close()
        }
        showTextViewShowOriginal()
    }

    private fun showTextViewShowOriginal() {
        Handler().postDelayed({
            binding.textShowOriginal.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.fade_in_main
                )
                visibility = View.VISIBLE
            }
        }, 400)
    }

    private fun showDialogTranslatorNotExits() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.bad_news))
            .setIcon(R.drawable.ic_sentiment_dissatisfied)
            .setMessage(resources.getString(R.string.languaje_not_available))
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
            }.show()
    }

    @SuppressLint("ShowToast")
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

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(
            binding.imgApodPicture,
            message,
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    private fun verifyDrawable() {
        if (binding.imgApodPicture.drawable == null) {
            showToastWaitToLoadImage()
        } else {
            if (apodReceived.media_type != "video") {
                when (isFirstTimeToOpenImage) {
                    0 -> {
                        showDialogWarningForResolutionImages()
                    }
                    1 -> {
                        navigateToPictureFragment()
                    }
                }
            }
        }

    }

    private fun showDialogWarningForResolutionImages() {
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
                navigateToPictureFragment()
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
        findNavController().navigate(R.id.action_detailsFragment_to_settingsActivity)
    }

    private fun navigateToPictureFragment() {
        viewModelShared.setArgsToPicture(
            PictureArgs(
                apodReceived.hdurl,
                apodReceived.url,
                apodReceived.title
            )
        )
        findNavController().navigate(R.id.action_detailsFragment_to_pictureFragment)
    }

    private fun updateViews() {
        setImage()
        setTexts()
        setDrawableOnFAB(apodReceived.is_favorite)
        if (apodReceived.media_type == "video") {
            showMessageInCaseOfVideo()
        }
    }

    private fun showMessageInCaseOfVideo() {
        binding.textVideoMessage.visibility = View.VISIBLE
        binding.iconCopyLink.visibility = View.VISIBLE
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
            binding.textApodCopyright.text = getString(R.string.no_copyright)
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
}