package com.listocalixto.dailycosmos.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

        dataStoreUtils.isDarkThemeActivated.observe(viewLifecycleOwner) {isActivated ->
            if (isActivated) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        dataStoreUtils.readValueFirstTime.observe(viewLifecycleOwner, {
            if (it == 0) {
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
            } else {
                if (firebaseAuth.currentUser != null) {
                    findNavController().navigate(R.id.action_splashFragment_to_mainActivity)
                    requireActivity().finish()
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}