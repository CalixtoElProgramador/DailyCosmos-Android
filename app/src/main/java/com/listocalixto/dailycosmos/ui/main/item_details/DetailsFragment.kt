package com.listocalixto.dailycosmos.ui.main.item_details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.remote.translator.TranslatorDataSource
import com.listocalixto.dailycosmos.databinding.FragmentDetailsBinding
import com.listocalixto.dailycosmos.presentation.translator.TranslatorDataStoreViewModel

@Suppress("DEPRECATION")
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var binding: FragmentDetailsBinding
    private val args by navArgs<DetailsFragmentArgs>()

    private lateinit var translatorDataStore: TranslatorDataStoreViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        setInformation()
        onClickImage()
        onClickButtonTranslate()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ShowToast")
    private fun onClickButtonTranslate() {
        binding.btnTranslate.setOnClickListener {
            translateExplanation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ShowToast")
    private fun translateExplanation() {
        translatorDataStore.readValue.observe(viewLifecycleOwner, {
            if (it == 0) {
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setTitle(getString(R.string.ask_download_traductor_title))
                    .setIcon(R.drawable.ic_save_alt)
                    .setMessage(resources.getString(R.string.ask_download_traductor))
                    .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                    }
                    .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
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
            } else {
                val translator =
                    TranslatorDataSource().downloadEnglishToOwnerLanguageModel(
                        requireContext(),
                        requireActivity()
                    )
                translator.translate(args.explanation).addOnSuccessListener { textTranslated ->
                    binding.textApodExplanation.text = textTranslated
                    translator.close()
                }
            }
        })
    }

    private fun onClickImage() {
        binding.imgApodPicture.setOnClickListener {
            if (binding.imgApodPicture.drawable == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.wait_for_the_image_to_load),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val action = DetailsFragmentDirections.actionDetailsFragmentToPictureFragment(
                    args.hdurl,
                    args.title,
                    args.url
                )
                findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setInformation() {
        if (args.isFavorite == 1) {
            binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
        } else {
            binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
        }
        if (args.hdurl.isEmpty()) {
            Glide.with(requireContext()).load(args.url).into(binding.imgApodPicture)
        } else {
            Glide.with(requireContext()).load(args.hdurl).into(binding.imgApodPicture)
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
        translatorDataStore =
            ViewModelProvider(requireActivity()).get(TranslatorDataStoreViewModel::class.java)
    }
}