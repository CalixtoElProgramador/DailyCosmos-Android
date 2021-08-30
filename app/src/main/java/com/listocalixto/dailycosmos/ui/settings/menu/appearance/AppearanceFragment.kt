package com.listocalixto.dailycosmos.ui.settings.menu.appearance

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentAppearanceBinding
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppearanceFragment : Fragment(R.layout.fragment_appearance) {

    private lateinit var binding: FragmentAppearanceBinding
    private val viewModelShared by activityViewModels<SettingsViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private var checkedItem = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAppearanceBinding.bind(view)

        binding.cardBrightness.setOnClickListener { showDialog() }

        viewModelShared.getDarkThemeMode().value?.let { mode ->
            checkedItem = mode
            when (mode) {
                0 -> {
                    binding.textDarkMode.text = getString(R.string.system_default)
                }
                1 -> {
                    binding.textDarkMode.text = getString(R.string.light)
                }
                2 -> {
                    binding.textDarkMode.text = getString(R.string.dark)
                }
            }
        }

    }

    private fun showDialog() {
        val singleItems = arrayOf(
            getString(R.string.system_default),
            getString(R.string.light),
            getString(R.string.dark)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.choose_a_theme))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                viewModelShared.setDarkThemeMode(checkedItem)
                dataStoreUtils.setDarkThemeMode(checkedItem)
                when (checkedItem) {
                    0 -> {
                        binding.textDarkMode.text = getString(R.string.system_default)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                        }
                    }
                    1 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        binding.textDarkMode.text = getString(R.string.light)
                    }
                    2 -> {
                        binding.textDarkMode.text = getString(R.string.dark)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                checkedItem = which
            }
            .show()
    }

}