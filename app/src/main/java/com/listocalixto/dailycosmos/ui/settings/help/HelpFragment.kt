package com.listocalixto.dailycosmos.ui.settings.help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentHelpBinding

class HelpFragment : Fragment(R.layout.fragment_help) {

    private lateinit var binding: FragmentHelpBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHelpBinding.bind(view)

        binding.cardContactMe.setOnClickListener { navigateToContactMeFragment() }
        binding.cardAppInfo.setOnClickListener { navigateToAppInfoFragment() }

    }

    private fun navigateToAppInfoFragment() {
        findNavController().navigate(R.id.action_helpFragment_to_appInfoFragment)
    }

    private fun navigateToContactMeFragment() {
        Snackbar.make(requireContext(), binding.cardAppInfo, getString(R.string.in_maintenance), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.use_email_app)) { intentEmail() }
            .show()
    }

    private fun intentEmail() {
        Toast.makeText(requireContext(), "Email", Toast.LENGTH_SHORT).show()
    }

}