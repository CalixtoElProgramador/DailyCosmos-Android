package com.listocalixto.dailycosmos.ui.settings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
import com.listocalixto.dailycosmos.ui.main.MainActivity
import kotlinx.coroutines.channels.BroadcastChannel


@Suppress("DEPRECATION")
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private var user: User? = null
    private val viewModel by activityViewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(AuthDataSource()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configWindow()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        configWindow()

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent("finish_activity")
            activity?.sendBroadcast(intent)
            findNavController().navigate(R.id.action_settingsFragment_to_authActivity)
            requireActivity().finish()
        }

        viewModel.getCurrentUser().observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    binding.lottieLoading.visibility = View.VISIBLE
                    Log.d("AuthViewModel", "Loading... ")
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    Log.d("AuthViewModel", "Results: ${result.data.toString()}")
                    if (result.data != null) {
                        user = result.data.also {
                            binding.textUserName.text = "${it.name} ${it.lastname}"
                            binding.textUserEmail.text = it.email
                            Glide.with(requireActivity()).load(it.photo_url).centerCrop()
                                .into(binding.imgUserPhoto)
                        }
                    }
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                    binding.textUserName.text = "Guest"
                    binding.textUserEmail.text = "guest@dailycosmos.com"
                    binding.imgUserPhoto.setImageResource(R.drawable.photo_cover)
                    Log.d("AuthViewModel", "Ocurrió un problema... Error: ${result.exception} ")
                }
            }
        })
    }

    private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity?.window?.statusBarColor =
            requireActivity().resources.getColor(R.color.colorPrimaryVariant)
    }

}