package com.listocalixto.dailycosmos.ui.auth.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import com.listocalixto.dailycosmos.databinding.FragmentLoginBinding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.domain.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.ui.auth.register.RegisterViewModel
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel

@Suppress("DEPRECATION")
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val patternEmail = Patterns.EMAIL_ADDRESS.toRegex()
    private lateinit var binding: FragmentLoginBinding
    private val viewModelShared: RegisterViewModel by activityViewModels()
    private val dataStoreUtils by viewModels<UtilsViewModel>()
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(AuthDataSource()))
    }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

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
                    Log.d("FirebaseAuth", "Usuario anónimo creado ")
                    navigateToMainActivity()
                } else {
                    isEnabledViews(true)
                    Log.d("FirebaseAuth", "Usuario anónimo NO creado: ${task.exception}")
                    Toast.makeText(
                        context,
                        "Ha ocurrido un error... inténtalo más tarde",
                        Toast.LENGTH_LONG
                    ).show()
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
                    navigateToMainActivity()

                }
                is Result.Failure -> {
                    isEnabledViews(true)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun navigateToMainActivity() {
        dataStoreUtils.saveValueFirstTime(1)
        findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
        requireActivity().finish()
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