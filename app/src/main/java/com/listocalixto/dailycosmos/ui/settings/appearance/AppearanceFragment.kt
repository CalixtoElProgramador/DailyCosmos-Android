package com.listocalixto.dailycosmos.ui.settings.appearance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentAppearanceBinding
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class AppearanceFragment : Fragment(R.layout.fragment_appearance) {

    private lateinit var binding: FragmentAppearanceBinding
    private val viewModelShared by activityViewModels<SettingsViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAppearanceBinding.bind(view)
        viewModelShared.isDarkTheme().value?.let {
            binding.switchDarkTheme.isChecked = it
        }

        binding.switchDarkTheme.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                dataStoreUtils.setDarkThemeActivated(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                dataStoreUtils.setDarkThemeActivated(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


    }

}