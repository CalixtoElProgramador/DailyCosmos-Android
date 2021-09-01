package com.listocalixto.dailycosmos.ui.settings.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.databinding.FragmentMenuBinding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : Fragment(R.layout.fragment_menu) {

    private val viewModelShared by activityViewModels<SettingsViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val viewModel by activityViewModels<AuthViewModel>()

    private var user: User? = null

    private lateinit var binding: FragmentMenuBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMenuBinding.bind(view)

        readPreferences()

        viewModelShared.getUser().value?.let {
            user = it
        }

        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
            setGuestInfo()
            binding.buttonSignOut.text = getString(R.string.create_an_account)
            binding.buttonSignOut.setOnClickListener { navigateToRegisterParentFragment() }
        } else {
            binding.buttonSignOut.text = getString(R.string.sign_out)
            binding.buttonSignOut.setOnClickListener { signOut() }
            if (user != null) {
                setUserInfo(user!!)
            } else {
                getUserInfo()
            }
        }

        binding.cardProfile.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }

        binding.cardNotifications.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardStorage.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardBrush.setOnClickListener { navigateToAppearance() }
        binding.cardHelp.setOnClickListener { navigateToHelp() }

        binding.cardPeople.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }
        binding.cardRateUs.setOnClickListener { showSnackbarMessage(getString(R.string.available_to_future_versions)) }

    }

    private fun navigateToRegisterParentFragment() {
        val activityNavHost = requireActivity().findViewById<View>(R.id.nav_host_activity)
        Navigation.findNavController(activityNavHost).navigate(R.id.action_settingsParentFragment_to_registerParentFragment)
    }

    private fun readPreferences() {
        dataStoreUtils.getDarkThemeMode.observe(viewLifecycleOwner, {
            viewModelShared.setDarkThemeMode(it)
        })
    }

    private fun navigateToAppearance() {
        findNavController().navigate(R.id.action_menuFragment_to_appearanceFragment)
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(requireContext(), binding.textCompanyName, message, Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun navigateToHelp() {
        findNavController().navigate(R.id.action_menuFragment_to_helpFragment)
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
                    when (result.exception) {
                        is FirebaseNetworkException -> {
                            showErrorSnackbarMessage(getString(R.string.error_internet_connection_login))
                        }
                        else -> {
                            showErrorSnackbarMessage(getString(R.string.error_something_went_wrong))
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("ShowToast")
    private fun showErrorSnackbarMessage(message: String) {
        val colorError = MaterialColors.getColor(requireView(), R.attr.colorError)
        Snackbar.make(binding.buttonSignOut, message, Snackbar.LENGTH_INDEFINITE)
            .setDuration(5000)
            .setBackgroundTint(colorError)
            .show()
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
        navigateToAuthActivity()
    }

    private fun navigateToAuthActivity() {
        val activityNavHost = requireActivity().findViewById<View>(R.id.nav_host_activity)
        Navigation.findNavController(activityNavHost)
            .navigate(R.id.action_settingsParentFragment_to_authParentFragment)
    }

}