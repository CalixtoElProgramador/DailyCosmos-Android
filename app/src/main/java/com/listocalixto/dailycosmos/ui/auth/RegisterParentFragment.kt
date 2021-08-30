package com.listocalixto.dailycosmos.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentParentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterParentFragment : Fragment(R.layout.fragment_parent_register) {

    private lateinit var binding: FragmentParentRegisterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentRegisterBinding.bind(view)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_register_fragment) as NavHostFragment
        val navController = navHostFragment.navController

    }
}