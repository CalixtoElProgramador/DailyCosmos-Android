package com.listocalixto.dailycosmos.ui.auth.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.remote.auth.UserDataSource
import com.listocalixto.dailycosmos.databinding.FragmentLoginBinding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.repository.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.ui.auth.register.RegisterViewModel
import com.listocalixto.dailycosmos.core.Result

@Suppress("DEPRECATION")
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val patternEmail = Patterns.EMAIL_ADDRESS.toRegex()
    private lateinit var binding: FragmentLoginBinding
    private val viewModelShared: RegisterViewModel by activityViewModels()
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(UserDataSource()))
    }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onResume() {
        super.onResume()
        getInputsFromViewModel()
    }

    private fun getInputsFromViewModel() {
        viewModelShared.apply {
            getPerson().value?.let { binding.inputEmail.setText(it.email) }
            getPassword().value?.let { binding.inputPassword.setText(it.passwrod) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        configWindow()
        isUserLoggedIn()
        setErrorEnabledAfterChanges()

        binding.buttonSignIn.setOnClickListener {
            validateInputs()
        }

        binding.textSingInAnon.setOnClickListener {
            createAnonCount()
            isEnabledViews(false)
            firebaseAuth.signInAnonymously().addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    isEnabledViews(true)
                    Log.d("FirebaseAuth", "Usuario anónimo creado ")
                    findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
                    requireActivity().finish()
                } else {
                    isEnabledViews(true)
                    Log.d("FirebaseAuth", "Usuario anónimo NO creado: ${task.exception}")
                    Toast.makeText(context, "Ha ocurrido un error... inténtalo más tarde", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.layoutTextRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_register01Fragment)
        }
    }

    private fun createAnonCount() {

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
                    findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
                    requireActivity().finish()

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

    private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS))
    }

    private fun isUserLoggedIn() {
        firebaseAuth.currentUser?.let {
            findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
            requireActivity().finish()
        }
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