package com.listocalixto.dailycosmos.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentParentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainParentFragment : Fragment(R.layout.fragment_parent_main) {

    private lateinit var binding: FragmentParentMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentMainBinding.bind(view)
        configWindow()
        
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        observeDestinationChange(navController)
    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
        }
    }
    private fun configWindow() {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}
