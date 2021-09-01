package com.listocalixto.dailycosmos.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import androidx.core.view.get
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

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        observeDestinationChange(navController)

    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todayFragment -> {
                    binding.bottomNavigation.menu[0].setIcon(R.drawable.ic_today)
                    binding.bottomNavigation.menu[1].setIcon(R.drawable.ic_explore_border)
                    binding.bottomNavigation.menu[2].setIcon(R.drawable.ic_favorite_border)

                }
                R.id.exploreFragment -> {
                    binding.bottomNavigation.menu[0].setIcon(R.drawable.ic_today_border)
                    binding.bottomNavigation.menu[1].setIcon(R.drawable.ic_explore)
                    binding.bottomNavigation.menu[2].setIcon(R.drawable.ic_favorite_border)
                }
                R.id.favoritesFragment -> {
                    binding.bottomNavigation.menu[0].setIcon(R.drawable.ic_today_border)
                    binding.bottomNavigation.menu[1].setIcon(R.drawable.ic_explore_border)
                    binding.bottomNavigation.menu[2].setIcon(R.drawable.ic_favorite)
                }
            }
        }
    }
}
