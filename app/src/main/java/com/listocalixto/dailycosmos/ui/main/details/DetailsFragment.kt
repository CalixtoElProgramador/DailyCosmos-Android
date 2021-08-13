package com.listocalixto.dailycosmos.ui.main.details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
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

@Suppress("DEPRECATION")
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var apodReceived: APOD
    private val args by navArgs<DetailsFragmentArgs>()

    private var isFavorite: Int = 0

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

    private lateinit var translatorViewModel: TranslatorViewModel
    private lateinit var utilsViewModel: UtilsViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        setInformation()

        binding.fabAddAPODFavorites.setOnClickListener { updateFavorite() }
        binding.btnTranslate.setOnClickListener { translateExplanation() }
        binding.imgApodPicture.setOnClickListener { verifyDrawable() }

    }

    private fun updateFavorite() {
        Log.d(
            "Details",
            "El valor que llega es: ${args.isFavorite} y el isFavorite es: $isFavorite"
        )
        when (isFavorite) {
            0 -> {
                viewModel.updateFavorite(apodReceived, 1)
                viewModelFavorite.setAPODFavorite(apodReceived)
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
                isFavorite = 1
            }
            1 -> {
                viewModel.updateFavorite(apodReceived, 0)
                viewModelFavorite.deleteFavorite(apodReceived)
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
                isFavorite = 0
            }
            -1 -> {
                viewModel.updateFavorite(apodReceived, 1)
                viewModelFavorite.setAPODFavorite(apodReceived)
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
                isFavorite = 1
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun translateExplanation() {
        translatorViewModel.readValue.observe(viewLifecycleOwner, {
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
                    translator.translate(args.title).addOnSuccessListener { titleTranslated ->
                        binding.textApodTitle.text = titleTranslated
                        translator.translate(args.explanation)
                            .addOnSuccessListener { textTranslated ->
                                binding.textApodExplanation.text = textTranslated
                                translator.close()
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
                translatorViewModel.saveValue(1)
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
                                navigateToPictureFragment()
                            }.show()
                    }
                    1 -> {
                        navigateToPictureFragment()
                    }
                }
            })
        }

    }

    private fun navigateToSettingsActivity() {
        findNavController().navigate(R.id.action_detailsFragment_to_settingsActivity)
    }

    private fun navigateToPictureFragment() {
        val action = DetailsFragmentDirections.actionDetailsFragmentToPictureFragment(
            args.hdurl,
            args.title,
            args.url
        )
        findNavController().navigate(action)
    }

    @SuppressLint("SetTextI18n")
    private fun setInformation() {
        when (isFavorite) {
            0 -> {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
            }
            1 -> {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
            }
        }

        when (args.mediaType) {
            "image" -> {
                if (args.hdurl.isEmpty()) {
                    Glide.with(requireContext()).load(args.url).into(binding.imgApodPicture)
                } else {
                    Glide.with(requireContext()).load(args.hdurl).into(binding.imgApodPicture)
                }
            }
            "video" -> {
                Glide.with(requireContext()).load(args.thumbnailUrl).into(binding.imgApodPicture)
            }
        }

        binding.textApodTitle.text = args.title
        binding.textApodDate.text = args.date
        if (args.explanation.isEmpty()) {
            binding.textApodExplanation.text = getString(R.string.no_description)
        } else {
            binding.textApodExplanation.text = args.explanation
        }
        if (args.copyright.isEmpty()) {
            binding.textApodCopyright.visibility = View.GONE
        } else {
            binding.textApodCopyright.text = "Copyright: ${args.copyright}"
        }
    }

    private fun initVars(view: View) {
        binding = FragmentDetailsBinding.bind(view)
        translatorViewModel =
            ViewModelProvider(requireActivity()).get(TranslatorViewModel::class.java)
        utilsViewModel = ViewModelProvider(requireActivity()).get(UtilsViewModel::class.java)
        isFavorite = args.isFavorite
        apodReceived = APOD(
            args.copyright,
            args.date,
            args.explanation,
            args.hdurl,
            args.mediaType,
            args.thumbnailUrl,
            args.title,
            args.url
        )
    }
}