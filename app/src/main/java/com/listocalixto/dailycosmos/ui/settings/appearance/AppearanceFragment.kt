package com.listocalixto.dailycosmos.ui.settings.appearance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentAppearanceBinding

class AppearanceFragment : Fragment(R.layout.fragment_appearance) {

    private lateinit var binding: FragmentAppearanceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAppearanceBinding.bind(view)

        binding.switchDarkTheme.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


    }

}