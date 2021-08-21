package com.listocalixto.dailycosmos.ui.settings.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import com.listocalixto.dailycosmos.databinding.FragmentSettingsBinding
import com.listocalixto.dailycosmos.domain.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel


@Suppress("DEPRECATION")
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private var user: User? = null
    private val viewModelShared by activityViewModels<SettingsViewModel>()
    private val viewModel by activityViewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(AuthDataSource()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        viewModelShared.getUser().value?.let {
            user = it
        }

        if (user == null) {
            getUserInfo()
        } else {
            setUserInfo(user!!)
        }

        binding.cardProfile.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }

        binding.cardNotifications.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardStorage.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardBrush.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardHelp.setOnClickListener { navigateToHelp() }

        binding.cardPeople.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardRateUs.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }

        binding.buttonSignOut.setOnClickListener { signOut() }

    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(requireContext(), binding.textCompanyName, message, Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun navigateToHelp() {
        findNavController().navigate(R.id.action_settingsFragment_to_helpFragment)
    }

    private fun getUserInfo() {
        viewModel.getCurrentUser().observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    binding.lottieLoading.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    if (result.data != null) {
                        user = result.data.also {
                            setUserInfo(it)
                            viewModelShared.setUser(it)
                        }
                    }
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                    setGuestInfo()
                }
            }
        })
    }

    private fun setGuestInfo() {
        binding.textUserName.text = getString(R.string.guest)
        binding.textUserEmail.text = getString(R.string.guest_email)
        binding.imgUserPhoto.setImageResource(R.drawable.photo_cover)
    }

    @SuppressLint("SetTextI18n")
    private fun setUserInfo(user: User) {
        binding.textUserName.text = "${user.name} ${user.lastname}"
        binding.textUserEmail.text = user.email
        Glide.with(requireActivity()).load(user.photo_url).centerCrop()
            .into(binding.imgUserPhoto)
    }

    private fun signOut() {
        Firebase.auth.signOut()
        finishMainActivity()
        navigateToAuthActivity()
    }

    private fun navigateToAuthActivity() {
        findNavController().navigate(R.id.action_settingsFragment_to_authActivity)
        requireActivity().finish()
    }

    private fun finishMainActivity() {
        val intent = Intent("finish_activity")
        activity?.sendBroadcast(intent)
    }


}