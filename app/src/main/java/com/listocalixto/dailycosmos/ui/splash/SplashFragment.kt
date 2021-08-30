package com.listocalixto.dailycosmos.ui.splash

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dataStoreUtils.getDarkThemeMode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    }

                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                2 -> {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                }
            }
        }

        dataStoreUtils.readValueFirstTime.observe(viewLifecycleOwner, {
            if (it == 0) {
                findNavController().navigate(R.id.action_splashFragment_to_welcomeParentFragment)
            } else {
                if (firebaseAuth.currentUser != null) {
                    findNavController().navigate(R.id.action_splashFragment_to_mainParentFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_authParentFragment)
                }
            }
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}