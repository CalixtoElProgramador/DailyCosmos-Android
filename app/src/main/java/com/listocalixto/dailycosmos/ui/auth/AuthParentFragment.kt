package com.listocalixto.dailycosmos.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentParentAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthParentFragment : Fragment(R.layout.fragment_parent_auth) {

    private lateinit var binding: FragmentParentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentAuthBinding.bind(view)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_login_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        observeDestinationChange(navController)

    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    if (binding.buttonsRegister.isVisible) {
                        binding.buttonsRegister.animation = AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_out_main
                        )
                        binding.buttonsRegister.visibility = View.GONE
                    }

                }
            }
        }
    }
}