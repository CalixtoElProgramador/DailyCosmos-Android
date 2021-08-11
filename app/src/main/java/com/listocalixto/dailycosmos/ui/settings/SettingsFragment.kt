package com.listocalixto.dailycosmos.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import com.listocalixto.dailycosmos.databinding.FragmentSettingsBinding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.domain.auth.AuthRepoImpl

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