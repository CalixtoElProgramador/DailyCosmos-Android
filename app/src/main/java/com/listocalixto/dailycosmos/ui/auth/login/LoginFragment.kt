package com.listocalixto.dailycosmos.ui.auth.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentLoginBinding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.ui.auth.RegisterViewModel
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val patternEmail = Patterns.EMAIL_ADDRESS.toRegex()
    private val viewModelShared by activityViewModels<RegisterViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()
    private val viewModel by activityViewModels<AuthViewModel>()
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var binding: FragmentLoginBinding

    override fun onResume() {
        super.onResume()
        getInputsFromViewModel()
    }

    private fun getInputsFromViewModel() {
        viewModelShared.apply {
            getPerson().value?.let { binding.inputEmail.setText(it.email) }
            getPassword().value?.let { binding.inputPassword.setText(it.password) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        setErrorEnabledAfterChanges()

        binding.buttonSignIn.setOnClickListener { validateInputs() }
        binding.textSingInAnon.setOnClickListener {
            isEnabledViews(false)
            firebaseAuth.signInAnonymously().addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    isEnabledViews(true)
                    navigateToMainParentFragment()
                } else {
                    isEnabledViews(true)
                    when(task.exception) {
                        is FirebaseNetworkException -> {
                            showErrorSnackbarMessage(getString(R.string.error_internet_connection_login))
                        }
                        else -> {
                            showErrorSnackbarMessage(getString(R.string.error_something_went_wrong))
                        }
                    }

                }
            }
        }

        binding.layoutTextRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_register01Fragment)
        }
    }

    private fun setErrorEnabledAfterChanges() {
        binding.inputPassword.doAfterTextChanged {
            binding.inputLayoutPassword.isErrorEnabled = false
        }
        binding.inputEmail.doAfterTextChanged {
            binding.inputLayoutEmail.isErrorEnabled = false
        }
    }

    private fun validateInputs() {
        when {
            binding.inputEmail.text.isNullOrEmpty() -> binding.inputLayoutEmail.error =
                getString(R.string.error_field_empty)
            !binding.inputEmail.text.toString()
                .matches(patternEmail) -> {
                binding.inputLayoutEmail.error = getString(R.string.error_email_no_valid)
            }
            binding.inputPassword.text.isNullOrEmpty() -> binding.inputLayoutPassword.error =
                getString(R.string.error_field_empty)
            binding.inputPassword.text.toString().length < 8 ->
                binding.inputLayoutPassword.error = getString(R.string.error_password_short)

            else -> {
                sendInputs()
            }
        }
    }

    private fun sendInputs() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        viewModel.signIn(email, password).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    isEnabledViews(false)
                }
                is Result.Success -> {
                    isEnabledViews(true)
                    navigateToMainParentFragment()

                }
                is Result.Failure -> {
                    isEnabledViews(true)
                    when(result.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            showErrorSnackbarMessage(getString(R.string.error_incorrect_password))
                        }
                        is FirebaseAuthInvalidUserException -> {
                            showErrorSnackbarMessage(getString(R.string.error_unregistered_email))
                        }
                        is FirebaseTooManyRequestsException -> {
                            showErrorSnackbarMessage(getString(R.string.error_too_many_requests))
                        }
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
        Snackbar.make(binding.textSingInAnon, message, Snackbar.LENGTH_INDEFINITE)
            .setDuration(5000)
            .setAnchorView(binding.layoutTextRegister)
            .setBackgroundTint(colorError)
            .show()
    }

    private fun navigateToMainParentFragment() {
        dataStoreUtils.saveValueFirstTime(1)
        val activityNavHost = requireActivity().findViewById<View>(R.id.nav_host_activity)
        Navigation.findNavController(activityNavHost).navigate(R.id.action_authParentFragment_to_mainParentFragment)

    }

    private fun isEnabledViews(boolean: Boolean) {
        binding.inputLayoutEmail.isEnabled = boolean
        binding.inputLayoutPassword.isEnabled = boolean
        binding.buttonSignIn.isEnabled = boolean
        binding.layoutTextRegister.isEnabled = boolean
        binding.textSingInAnon.isEnabled = boolean

        if (!boolean) {
            binding.lottieLoading.visibility = View.VISIBLE
        } else {
            binding.lottieLoading.visibility = View.GONE
        }

    }

}