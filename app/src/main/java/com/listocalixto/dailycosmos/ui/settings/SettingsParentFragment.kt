package com.listocalixto.dailycosmos.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentParentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsParentFragment : Fragment(R.layout.fragment_parent_settings) {

    private lateinit var binding: FragmentParentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentSettingsBinding.bind(view)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_settings_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        observeDestinationChange(navController)

        binding.topAppBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.menuFragment -> {setToolbarTitle(getString(R.string.settings))}
                R.id.helpFragment -> {setToolbarTitle(getString(R.string.help))}
                R.id.contactMeFragment -> {setToolbarTitle(getString(R.string.contact_me))}
                R.id.appInfoFragment -> {setToolbarTitle(getString(R.string.application_info))}
                R.id.appearanceFragment -> {setToolbarTitle(getString(R.string.appearance))}
            }
        }
    }

    private fun setToolbarTitle(title: String) {
        binding.topAppBar.title = title
    }

}